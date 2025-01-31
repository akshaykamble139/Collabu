import React from "react";
import { AppBar, Toolbar, Typography, Button, Box } from "@mui/material";
import { Link, useNavigate } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import { logout } from "../redux/userSlice";

const Header = () => {
  const userData = useSelector((state) => state.user);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleLogout = () => {
    dispatch(logout());
    navigate("/login");
  };

  return (
    <AppBar position="static" sx={{ boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)' }}>
      <Toolbar>
        <Typography
          variant="h6"
          component={Link}
          to="/"
          sx={{
            flexGrow: 1,
            textDecoration: "none",
            color: "inherit",
            fontWeight: "bold",
          }}
        >
          Collabu
        </Typography>
        <Box>
          {userData?.username ? (
            <>
              <Button
                color="inherit"
                component={Link}
                to={`/profile/${userData.username}`}
                sx={{ textTransform: "none" }}
              >
                Profile
              </Button>
              <Button
                color="inherit"
                component={Link}
                to={`/${userData.username}/repositories`}
                sx={{ textTransform: "none" }}
              >
                Repositories
              </Button>
              <Button
                color="inherit"
                onClick={handleLogout}
                sx={{ textTransform: "none" }}
              >
                Logout
              </Button>
            </>
          ) : (
            <>
              <Button
                color="inherit"
                component={Link}
                to="/login"
                sx={{ textTransform: "none" }}
              >
                Login
              </Button>
              <Button
                color="inherit"
                component={Link}
                to="/register"
                sx={{ textTransform: "none" }}
              >
                Register
              </Button>
            </>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;
