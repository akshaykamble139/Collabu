import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useLocation } from "react-router-dom";
import { setNavigationDetails } from "../redux/navigationSlice";

const NavigationHandler = () => {
    const location = useLocation();
    const dispatch = useDispatch();
    const userData = useSelector(state => state.user);

    useEffect(() => {
        const path = location.pathname;

        let pageName = "Error Page";
        let repoUsername = "";
        let repoName = "";
        let repoBranchName = "main";
        let currentPath = "/";
        let fileName = "";

        if (path === "/") {
            pageName = "Home Page";
            if (userData?.username) {
                repoUsername = userData.username;
            }
        } else if (path.startsWith("/login")) {
            pageName = "Login Page";
        } else if (path.startsWith("/register")) {
            pageName = "Register Page";
        } else if (path.includes("blob")) {
            const [base, details] = path.split("/blob/");
            [repoUsername, repoName] = base.split("/").slice(1, 3);
            [repoBranchName, ...fileName] = details.split("/");
            currentPath = "/" + fileName.slice(0, -1).join("/");
            fileName = fileName[fileName.length - 1];
            pageName = repoBranchName + " Branch - File";
        } else if (path.includes("tree")) {
            const [base, details] = path.split("/tree/");
            [repoUsername, repoName] = base.split("/").slice(1, 3);
            [repoBranchName, ...currentPath] = details.split("/");
            currentPath = "/" + currentPath.join("/");
            pageName = currentPath === "/" ? repoBranchName + " Branch - Root Directory" : repoBranchName + " Branch - Nested Folder";
        } else if (path.endsWith("/repositories")) {
            pageName = "Repositories List Page";
            repoUsername = path.split("/")[1]
        } else if (path.endsWith("/branches")) {
            pageName = "Branches List Page";
            [repoUsername, repoName] = path.split("/").slice(1,3)
        } else if (path.startsWith("/profile")) {
            pageName = "Profile Page";
            if (userData?.username) {
                repoUsername = userData.username;
            }
        } else {
            [repoUsername, repoName] = path.split("/").slice(1);
            repoBranchName = "main";
            currentPath = "/";
            pageName = "main Branch - Root Directory";
        }

        if (currentPath !== "" && !currentPath.endsWith("/")) {
            currentPath += "/";
        }

        dispatch(
            setNavigationDetails({
                repoUsername,
                repoName,
                repoBranchName,
                currentPath,
                fileName,
                pageName,
            })
        );
    }, [location, userData, dispatch]);

    return null; // This component doesn't render anything
};

export default NavigationHandler;
