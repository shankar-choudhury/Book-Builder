import React from "react";
import { register } from "../auth";
import { errorMessage } from "../errors";

export function RegisterPage({ onGoLogin }: { onGoLogin: () => void }) {
  const [username, setUsername] = React.useState("");
  const [email, setEmail] = React.useState("");
  const [password, setPassword] = React.useState("");
  const [status, setStatus] = React.useState<string>("");

  async function handleRegister(e: React.FormEvent) {
    e.preventDefault();
    setStatus("Registering...");
    try {
      await register({ username, email, password });
      setStatus("Registered. Redirecting to login...");
      onGoLogin();
    } catch (err: unknown) {
      setStatus(errorMessage(err));
    }
  }

  return (
    <div
      style={{ maxWidth: 420, margin: "40px auto", fontFamily: "system-ui" }}
    >
      <h1>Register</h1>

      <form onSubmit={handleRegister} style={{ display: "grid", gap: 12 }}>
        <label>
          Username
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            style={{ width: "100%" }}
          />
        </label>

        <label>
          Email
          <input
            value={email}
            onChange={(e) => setEmail(e.target.value)}
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

        <button type="submit">Create account</button>
      </form>

      <p style={{ marginTop: 12 }}>{status}</p>

      <button onClick={onGoLogin} style={{ marginTop: 12 }}>
        Already have an account? Go to Login
      </button>
    </div>
  );
}
