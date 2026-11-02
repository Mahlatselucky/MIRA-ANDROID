require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const { OAuth2Client } = require('google-auth-library');
const admin = require('firebase-admin');
const { User, Post, Comment } = require('./models');

const app = express();
app.use(cors());
app.use(express.json());

// 1. Initialize Firebase Admin from Render Environment Variable
if (process.env.FIREBASE_SERVICE_ACCOUNT) {
  const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
} else {
  console.warn('FIREBASE_SERVICE_ACCOUNT env variable not set yet.');
}

// 2. Initialize Google OAuth Client
const googleClient = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

// 3. Connect to MongoDB Atlas
mongoose.connect(process.env.MONGO_URI)
  .then(() => console.log('Connected to MongoDB Atlas'))
  .catch(err => console.error('MongoDB connection error:', err));

// Health check route so you can test the URL in your browser
app.get('/', (req, res) => {
  res.send('MIRA Backend is running!');
});

// --- 1. GOOGLE SIGN-IN VERIFICATION & TOKEN ISSUING ---
app.post('/api/auth/google', async (req, res) => {
  const { idToken, fcmToken } = req.body;

  try {
    const ticket = await googleClient.verifyIdToken({
      idToken: idToken,
      audience: process.env.GOOGLE_CLIENT_ID,
    });

    const payload = ticket.getPayload();
    const { sub: googleId, email, name } = payload;

    let user = await User.findOne({ email });
    if (!user) {
      user = await User.create({
        email,
        name: name || email.split('@')[0],
        googleId,
        fcmToken: fcmToken || null
      });
    } else {
      user.googleId = googleId;
      if (fcmToken) user.fcmToken = fcmToken;
      await user.save();
    }

    const token = jwt.sign(
      { userId: user._id, email: user.email, name: user.name },
      process.env.JWT_SECRET,
      { expiresIn: '7d' }
    );

    res.status(200).json({ token, user });
  } catch (error) {
    console.error('Google Auth Error:', error);
    res.status(401).json({ error: 'Invalid Google ID Token' });
  }
});

// Update user's FCM token separately if needed
app.put('/api/users/:userId/fcm-token', async (req, res) => {
  try {
    const { fcmToken } = req.body;
    await User.findByIdAndUpdate(req.params.userId, { fcmToken });
    res.status(200).json({ message: 'FCM token updated' });
  } catch (error) {
    res.status(500).json({ error: 'Failed to update FCM token' });
  }
});

// --- 2. ADD COMMENT & TRIGGER FIREBASE PUSH NOTIFICATION ---
app.post('/api/posts/:postId/comments', async (req, res) => {
  const { postId } = req.params;
  const { text, authorName, commenterId } = req.body;

  try {
    const comment = await Comment.create({
      postId,
      authorName: authorName || 'Someone',
      text
    });

    const post = await Post.findById(postId).populate('authorId');

    if (
      post &&
      post.authorId &&
      post.authorId.fcmToken &&
      post.authorId._id.toString() !== commenterId
    ) {
      const message = {
        notification: {
          title: 'New Comment on MIRA',
          body: `${comment.authorName} commented on "${post.title}"`
        },
        data: {
          type: 'COMMENT',
          postId: post._id.toString()
        },
        token: post.authorId.fcmToken
      };

      await admin.messaging().send(message);
      console.log('Push notification sent to post owner');
    }

    res.status(201).json(comment);
  } catch (error) {
    console.error('Comment / Notification Error:', error);
    res.status(500).json({ error: 'Failed to post comment' });
  }
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));