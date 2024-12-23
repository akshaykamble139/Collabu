import React from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Button, Typography, Container, Box } from "@mui/material";

const errorMessages = {
  404: "Oops! The page you are looking for does not exist.",
  500: "Something went wrong. Please try again later.",
  403: "You do not have permission to access this page.",
  401: "Unauthorized access. Please log in.",
  400: "Bad Request. Please check the request and try again.",
  default: "An unexpected error occurred.",
};

const ErrorPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { code } = location.state || { code: 500 };  // Default to 500 if no code is passed

  const message = errorMessages[code] || errorMessages.default;

  const handleBackHome = () => {
    navigate("/");  // Redirect to Home Page
  };

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
        <Typography variant="h1" component="h1" color="error" gutterBottom>
          {code}
        </Typography>
        <Typography variant="h5" gutterBottom>
          {message}
        </Typography>
        <Button
          variant="contained"
          color="primary"
          onClick={handleBackHome}
          sx={{ mt: 3 }}
        >
          Go Back Home
        </Button>
      </Box>
    </Container>
  );
};

export default ErrorPage;
