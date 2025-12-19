import React from "react";
import { login, me } from "../auth";
import { errorMessage } from "../errors";

export function LoginPage({
  onAuthed,
  onGoRegister,
}: {
  onAuthed: () => void;
  onGoRegister: () => void;
}) {
  const [username, setUsername] = React.useState("user1");
  const [password, setPassword] = React.useState("pswd1");
  const [status, setStatus] = React.useState<string>("");

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setStatus("Logging in...");
    try {
      await login({ username, password });
      // Hit an authenticated GET to mint CSRF cookie
      await me();
      setStatus("Logged in.");
      onAuthed();
    } catch (err: unknown) {
      setStatus(errorMessage(err));
    }
  }

  return (
    <div
      style={{ maxWidth: 420, margin: "40px auto", fontFamily: "system-ui" }}
    >
      <h1>Login</h1>

      <form onSubmit={handleLogin} style={{ display: "grid", gap: 12 }}>
        <label>
          Username
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            style={{ width: "100%" }}
          />
        </label>

        <label>
          Password
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={{ width: "100%" }}
          />
        </label>

        <button type="submit">Login</button>
      </form>

      <p style={{ marginTop: 12 }}>{status}</p>

      <button onClick={onGoRegister} style={{ marginTop: 12 }}>
        Need an account? Register
      </button>
    </div>
  );
}
