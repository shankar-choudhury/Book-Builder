import React from "react";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { MePage } from "./pages/MePage";

type View = "login" | "register" | "me";

export default function App() {
  const [view, setView] = React.useState<View>("login");

  if (view === "me") {
    return <MePage onLoggedOut={() => setView("login")} />;
  }

  if (view === "register") {
    return <RegisterPage onGoLogin={() => setView("login")} />;
  }

  return (
    <LoginPage
      onAuthed={() => setView("me")}
      onGoRegister={() => setView("register")}
    />
  );
}
