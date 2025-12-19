import React from "react";
import { me, refresh, logout, updateUser } from "../auth";
import { errorMessage } from "../errors";
import type { UserDto } from "../types";

export function MePage({ onLoggedOut }: { onLoggedOut: () => void }) {
  const [user, setUser] = React.useState<UserDto | null>(null);
  const [status, setStatus] = React.useState<string>("");

  const [email, setEmail] = React.useState<string>("");
  const [password, setPassword] = React.useState<string>("");

  async function loadMe() {
    setStatus("Loading /users/me ...");
    try {
      const u = await me();
      setUser(u);
      setEmail(u.email);
      setStatus("Loaded.");
    } catch (err: unknown) {
      setStatus(errorMessage(err));
      setUser(null);
    }
  }

  React.useEffect(() => {
    void loadMe();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleRefresh() {
    setStatus("Refreshing...");
    try {
      await refresh();
      await loadMe();
      setStatus("Refreshed (new access/refresh cookies).");
    } catch (err: unknown) {
      setStatus(errorMessage(err));
    }
  }

  async function handleLogout() {
    setStatus("Logging out...");
    try {
      await logout();
      setStatus("Logged out.");
      onLoggedOut();
    } catch (err: unknown) {
      setStatus(errorMessage(err));
    }
  }

  async function handleUpdate() {
    if (!user) return;
    setStatus("Updating...");
    try {
      const updated = await updateUser({
        username: user.username,
        email,
        password: password || "pass1",
      });
      setUser(updated);
      setStatus("Updated (CSRF header sent automatically).");
    } catch (err: unknown) {
      setStatus(errorMessage(err));
    }
  }

  return (
    <div
      style={{ maxWidth: 700, margin: "40px auto", fontFamily: "system-ui" }}
    >
      <h1>Account</h1>

      <div
        style={{ display: "flex", gap: 12, marginBottom: 12, flexWrap: "wrap" }}
      >
        <button onClick={loadMe}>Reload /users/me</button>
        <button onClick={handleRefresh}>POST /auth/refresh</button>
        <button onClick={handleLogout}>POST /logout</button>
      </div>

      <p>{status}</p>

      {user ? (
        <>
          <h2>Authenticated User</h2>
          <pre style={{ padding: 12, background: "#f5f5f5", borderRadius: 8 }}>
            {JSON.stringify(user, null, 2)}
          </pre>

          <h2>Update</h2>
          <div style={{ display: "grid", gap: 12 }}>
            <label>
              Email
              <input
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                style={{ width: "100%" }}
              />
            </label>

            <label>
              Password (demo)
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{ width: "100%" }}
              />
            </label>

            <button onClick={handleUpdate}>PUT /users/update</button>
          </div>
        </>
      ) : (
        <p>Not authenticated (or cookies/CORS blocked).</p>
      )}
    </div>
  );
}
