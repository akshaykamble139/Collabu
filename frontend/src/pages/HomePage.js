// src/pages/HomePage.js
import React from "react";
import { Link } from "react-router-dom";
import { Button, Typography, Container, Box } from "@mui/material";

const HomePage = () => {
  return (
    <Container maxWidth="md">
      <Box
        sx={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          minHeight: "100vh",
          textAlign: "center",
        }}
      >
        <Typography variant="h2" component="h1" gutterBottom>
          Welcome to Collabu
        </Typography>
        <Typography variant="h6" gutterBottom>
          A modern platform for code collaboration and version control.
        </Typography>
        <Box sx={{ mt: 4 }}>
          <Link to="/login" style={{ textDecoration: "none", marginRight: 16 }}>
            <Button variant="contained" color="primary" size="large">
              Login
            </Button>
          </Link>
          <Link to="/register" style={{ textDecoration: "none" }}>
            <Button variant="outlined" color="primary" size="large">
              Register
            </Button>
          </Link>
        </Box>
      </Box>
    </Container>
  );
};

export default HomePage;
