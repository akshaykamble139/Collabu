import React, { useEffect, useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import Dashboard from "./pages/Dashboard";
import HomePage from "./pages/HomePage";
import RegisterPage from "./pages/RegisterPage";
import ProfilePage from "./pages/ProfilePage";
import RepositoriesPage from "./pages/RepositoriesPage";
import ErrorPage from "./pages/ErrorPage";
import { useDispatch, useSelector } from "react-redux";
import { setUser } from "./redux/userSlice";

const App = () => {
  const userData = useSelector(state => state.user);
  const [isUserLoggedIn, setUserLogin] = useState(false);

  const dispatch = useDispatch();
  useEffect(() => {
    const token = localStorage.getItem("token");
    const username = localStorage.getItem("username");

  if (typeof(token) !== undefined && typeof(username) != undefined && token && username) {
    dispatch(setUser({username: username, token: token}))
  }
  else {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
  }
  },[])

  useEffect(() => {
    setUserLogin(userData !== null && userData.username !== "" && userData.token !== "");
  },[userData]);

  return (
    <Router>
      <Routes>
        <Route path="/" element={!isUserLoggedIn ? <HomePage /> : <RepositoriesPage />} /> 
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/:username/repositories" element={<RepositoriesPage />} />
        <Route path="*" element={<ErrorPage />} /> 
        </Routes>
    </Router>
  );
};

export default App;
