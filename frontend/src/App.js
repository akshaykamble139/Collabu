import React, { useEffect, useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import HomePage from "./pages/HomePage";
import RegisterPage from "./pages/RegisterPage";
import ProfilePage from "./pages/ProfilePage";
import RepositoriesPage from "./pages/repository/RepositoriesPage";
import ErrorPage from "./pages/ErrorPage";
import { useSelector } from "react-redux";
import Header from "./globalComponents/Header";
import RepositoryPage from "./pages/repository/RepositoryPage";
import GlobalNotification from "./globalComponents/GlobalNotification";
import ReactivateAccount from "./pages/ReactivateAccount";
import BranchesPage from "./pages/BranchesPage";
import ConfirmationDialog from "./globalComponents/ConfirmationDialog";
import { ConfirmationDialogProvider } from "./globalComponents/ConfirmationDialogContext";
import NavigationHandler from "./globalComponents/NavigationHandler";

const App = () => {
  const userData = useSelector(state => state.user);
  const [isUserLoggedIn, setUserLogin] = useState(false);

  useEffect(() => {
    setUserLogin(userData !== null && userData.username !== "" && userData.token !== "");
  },[userData]);

  return (
    <Router>
      <ConfirmationDialogProvider>
      <NavigationHandler />
      <Header isUserLoggedIn={isUserLoggedIn} username={userData?.username} />
      <GlobalNotification />  
      <ConfirmationDialog />
      <Routes>
        <Route path="/" element={!isUserLoggedIn ? <HomePage /> : <RepositoriesPage />} /> 
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/reactivate" element={<ReactivateAccount />} />
        <Route path="/profile/:username" element={<ProfilePage />} />
        <Route path="/:username/repositories" element={<RepositoriesPage />} />
        <Route path="/:username/:repoName/branches" element={<BranchesPage />} />
        <Route path="/:username/:repoName/tree/:branchName/*" element={<RepositoryPage />} />
        <Route path="/:username/:repoName/tree/:branchName" element={<RepositoryPage />} />
        <Route path="/:username/:repoName/blob/:branchName/*" element={<RepositoryPage />} />
        <Route path="/:username/:repoName" element={<RepositoryPage />} />
        <Route path="*" element={<ErrorPage />} /> 
        </Routes>
        </ConfirmationDialogProvider>
    </Router>
  );
};

export default App;
