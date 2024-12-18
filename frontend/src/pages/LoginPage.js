import React, { useState } from "react";
import axios from "axios";
import { useDispatch, useSelector } from "react-redux";
import { setUser } from "../redux/userSlice";

const LoginPage = () => {
  const [form, setForm] = useState({ username: "", password: "" });
  const user = useSelector(state => state.user);
  const dispatch = useDispatch();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post("http://localhost:8080/collabu/api/auth/login", form);
      localStorage.setItem("token", response.data); // Save token to localStorage
      dispatch(setUser({"username": form.username, "token": response.data}));
      alert("Login successful");
    } catch (error) {
      alert("Login failed");
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        placeholder="Username"
        value={form.username}
        onChange={(e) => setForm({ ...form, username: e.target.value })}
      />
      <input
        type="password"
        placeholder="Password"
        value={form.password}
        onChange={(e) => setForm({ ...form, password: e.target.value })}
      />
      <button type="submit">Login</button>
    </form>
  );
};

export default LoginPage;
