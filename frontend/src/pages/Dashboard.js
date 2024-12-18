import React, { useEffect, useState } from "react";
import axios from "axios";
import { useSelector } from "react-redux";

const Dashboard = () => {
  const [repos, setRepos] = useState([]);
//   const token = useSelector(state => state.user.token);

//   useEffect(() => {
//     const fetchRepositories = async () => {
//       const token = localStorage.getItem("token");
//       const response = await axios.get("http://localhost:8080/collabu/repositories", {
//         headers: { Authorization: `Bearer ${token}` },
//       });
//       setRepos(response.data);
//     };

//     fetchRepositories();
//   }, []);

  return (
    <div>
      <h1>My Repositories</h1>
      <ul>
        {repos.map((repo) => (
          <li key={repo.id}>{repo.name}</li>
        ))}
      </ul>
    </div>
  );
};

export default Dashboard;
