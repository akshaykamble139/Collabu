import React, { useEffect, useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import HomePage from "./pages/HomePage";
import RegisterPage from "./pages/RegisterPage";
import ProfilePage from "./pages/ProfilePage";
import RepositoriesPage from "./pages/RepositoriesPage";
import ErrorPage from "./pages/ErrorPage";
import { useDispatch, useSelector } from "react-redux";
import { setUser } from "./redux/userSlice";
import Header from "./pages/Header";
import RepositoryPage from "./pages/RepositoryPage";
import GlobalNotification from "./pages/GlobalNotification";
import ReactivateAccount from "./pages/ReactivateAccount";

const App = () => {
  const userData = useSelector(state => state.user);
  const [isUserLoggedIn, setUserLogin] = useState(false);

  const dispatch = useDispatch();
  useEffect(() => {
    const token = localStorage.getItem("token");
    const username = localStorage.getItem("username");

  if (typeof(token) !== undefined && typeof(username) != undefined && token && username) {
    dispatch(setUser({username: username, token: token}));
    console.log("userdata set",userData);
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
      <Header isUserLoggedIn={isUserLoggedIn} username={userData?.username} />
      <GlobalNotification />  
      <Routes>
        <Route path="/" element={!isUserLoggedIn ? <HomePage /> : <RepositoriesPage />} /> 
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/reactivate" element={<ReactivateAccount />} />
        <Route path="/profile/:username" element={<ProfilePage />} />
        <Route path="/:username/repositories" element={<RepositoriesPage />} />
        <Route path="/:username/:repoName" element={<RepositoryPage />} />
        <Route path="*" element={<ErrorPage />} /> 
        </Routes>
    </Router>
  );
};

export default App;
