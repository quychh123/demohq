const express = require('express');
const crypto = require('crypto');
const fs = require('fs/promises');
const path = require('path');

const app = express();
const PORT = Number(process.env.PORT) || 10000;
const DATA_DIR = path.resolve(process.env.DATA_DIR || path.join(__dirname, 'data'));
const SNAPSHOTS_DIR = path.resolve(DATA_DIR, 'snapshots');
const USERS_FILE = path.resolve(DATA_DIR, 'users.json');
const SESSION_SECRET = process.env.SESSION_SECRET || crypto.randomBytes(32).toString('hex');
const TOKEN_TTL_MS = 30 * 24 * 60 * 60 * 1000;

app.use(express.json({ limit: '5mb' }));

async function ensureStorage() {
  await fs.mkdir(SNAPSHOTS_DIR, { recursive: true });
  try {
    await fs.access(USERS_FILE);
  } catch {
    await fs.writeFile(USERS_FILE, '[]', 'utf8');
  }
}

function resolveSnapshotPath(userId) {
  if (!/^[a-f0-9-]{8,64}$/i.test(userId)) {
    throw new Error('Invalid userId');
  }
  const target = path.resolve(SNAPSHOTS_DIR, `${userId}.hqbak.json`);
  if (!target.startsWith(SNAPSHOTS_DIR + path.sep)) {
    throw new Error('Invalid storage path');
  }
  return target;
}

async function readUsers() {
  await ensureStorage();
  const raw = await fs.readFile(USERS_FILE, 'utf8');
  const parsed = JSON.parse(raw);
  return Array.isArray(parsed) ? parsed : [];
}

async function writeUsers(users) {
  await ensureStorage();
  const tmpFile = path.resolve(DATA_DIR, `users.${crypto.randomUUID()}.tmp`);
  if (!tmpFile.startsWith(DATA_DIR + path.sep)) {
    throw new Error('Invalid users temp path');
  }
  await fs.writeFile(tmpFile, JSON.stringify(users, null, 2), 'utf8');
  await fs.rename(tmpFile, USERS_FILE);
}

function hashPassword(password, saltHex = crypto.randomBytes(16).toString('hex')) {
  const derived = crypto.scryptSync(password, saltHex, 64).toString('hex');
  return `${saltHex}:${derived}`;
}

function verifyPassword(password, storedHash) {
  const parts = String(storedHash).split(':');
  if (parts.length !== 2) return false;
  const [saltHex, expectedHex] = parts;
  const actualBuf = crypto.scryptSync(password, saltHex, 64);
  const expectedBuf = Buffer.from(expectedHex, 'hex');
  if (actualBuf.length !== expectedBuf.length) return false;
  return crypto.timingSafeEqual(actualBuf, expectedBuf);
}

function signToken(user) {
  const payload = Buffer.from(
    JSON.stringify({
      uid: user.id,
      email: user.email,
      exp: Date.now() + TOKEN_TTL_MS,
    })
  ).toString('base64url');
  const sig = crypto.createHmac('sha256', SESSION_SECRET).update(payload).digest('base64url');
  return `${payload}.${sig}`;
}

function verifyToken(token) {
  const parts = String(token || '').split('.');
  if (parts.length !== 2) return null;
  const [payloadB64, sigB64] = parts;
  const expectedSig = crypto
    .createHmac('sha256', SESSION_SECRET)
    .update(payloadB64)
    .digest('base64url');
  const sigBuf = Buffer.from(sigB64);
  const expectedBuf = Buffer.from(expectedSig);
  if (sigBuf.length !== expectedBuf.length || !crypto.timingSafeEqual(sigBuf, expectedBuf)) {
    return null;
  }
  try {
    const parsed = JSON.parse(Buffer.from(payloadB64, 'base64url').toString('utf8'));
    if (!parsed.uid || !parsed.exp || Date.now() > parsed.exp) return null;
    return parsed;
  } catch {
    return null;
  }
}

async function requireAuth(req, res, next) {
  const authHeader = req.get('Authorization') || '';
  if (!authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Thiếu token xác thực' });
  }
  const token = authHeader.slice(7).trim();
  const claims = verifyToken(token);
  if (!claims) {
    return res.status(401).json({ error: 'Phiên đăng nhập không hợp lệ hoặc đã hết hạn' });
  }
  const users = await readUsers();
  const user = users.find((u) => u.id === claims.uid);
  if (!user) {
    return res.status(401).json({ error: 'Tài khoản không tồn tại' });
  }
  req.user = user;
  next();
}

app.get('/health', (_req, res) => {
  res.json({
    ok: true,
    service: 'HQ Apple & Kho Hang Min Render Sync Server',
    timestampMillis: Date.now(),
  });
});

app.post('/api/auth/register', async (req, res) => {
  try {
    const email = String(req.body?.email || '').trim().toLowerCase();
    const password = String(req.body?.password || '');
    const shopName = String(req.body?.shopName || 'HQ Apple & Kho Hàng Min').trim().slice(0, 80);

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      return res.status(400).json({ error: 'Email không đúng định dạng' });
    }
    if (password.length < 6 || password.length > 128) {
      return res.status(400).json({ error: 'Mật khẩu phải từ 6 đến 128 ký tự' });
    }

    const users = await readUsers();
    if (users.some((u) => u.email === email)) {
      return res.status(409).json({ error: 'Email này đã được đăng ký' });
    }

    const user = {
      id: crypto.randomUUID(),
      email,
      shopName: shopName || 'HQ Apple & Kho Hàng Min',
      passwordHash: hashPassword(password),
      createdAtMillis: Date.now(),
    };
    users.push(user);
    await writeUsers(users);

    const token = signToken(user);
    res.status(201).json({
      token,
      user: { id: user.id, email: user.email, shopName: user.shopName },
    });
  } catch (err) {
    console.error('Register error:', err.message);
    res.status(500).json({ error: 'Không thể đăng ký tài khoản' });
  }
});

app.post('/api/auth/login', async (req, res) => {
  try {
    const email = String(req.body?.email || '').trim().toLowerCase();
    const password = String(req.body?.password || '');
    const users = await readUsers();
    const user = users.find((u) => u.email === email);

    if (!user || !verifyPassword(password, user.passwordHash)) {
      return res.status(401).json({ error: 'Sai email hoặc mật khẩu' });
    }

    const token = signToken(user);
    res.json({
      token,
      user: { id: user.id, email: user.email, shopName: user.shopName },
    });
  } catch (err) {
    console.error('Login error:', err.message);
    res.status(500).json({ error: 'Không thể đăng nhập' });
  }
});

app.post('/api/sync/push', requireAuth, async (req, res) => {
  try {
    const snapshot = req.body?.snapshot;
    if (!snapshot || snapshot.format !== 'HQBAK_V1' || !snapshot.data) {
      return res.status(400).json({ error: 'Dữ liệu đồng bộ không đúng chuẩn HQBAK_V1' });
    }

    const targetPath = resolveSnapshotPath(req.user.id);
    const tmpPath = path.resolve(SNAPSHOTS_DIR, `${req.user.id}.${crypto.randomUUID()}.tmp`);
    if (!tmpPath.startsWith(SNAPSHOTS_DIR + path.sep)) {
      throw new Error('Invalid temp snapshot path');
    }

    const syncedAtMillis = Date.now();
    const record = {
      syncedAtMillis,
      snapshot,
    };
    await fs.writeFile(tmpPath, JSON.stringify(record), 'utf8');
    await fs.rename(tmpPath, targetPath);

    res.json({ ok: true, syncedAtMillis });
  } catch (err) {
    console.error('Push error:', err.message);
    res.status(500).json({ error: 'Không thể lưu dữ liệu đồng bộ' });
  }
});

app.get('/api/sync/pull', requireAuth, async (req, res) => {
  try {
    const targetPath = resolveSnapshotPath(req.user.id);
    try {
      const raw = await fs.readFile(targetPath, 'utf8');
      const parsed = JSON.parse(raw);
      return res.json({
        ok: true,
        syncedAtMillis: parsed.syncedAtMillis || null,
        snapshot: parsed.snapshot || null,
      });
    } catch {
      return res.json({ ok: true, syncedAtMillis: null, snapshot: null });
    }
  } catch (err) {
    console.error('Pull error:', err.message);
    res.status(500).json({ error: 'Không thể tải dữ liệu đồng bộ' });
  }
});

ensureStorage()
  .then(() => {
    app.listen(PORT, () => {
      console.log(`HQ Apple & Kho Hang Min Render Sync Server listening on port ${PORT}`);
    });
  })
  .catch((err) => {
    console.error('Failed to initialize storage:', err);
    process.exit(1);
  });
