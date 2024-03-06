db = db.getSiblingDB('stat');

db.createCollection("users");

db.createUser({
  user: "root",
  pwd: "example",
  roles: [
    {
      role: "readWrite",
      db: "stat",
    },
  ],
});