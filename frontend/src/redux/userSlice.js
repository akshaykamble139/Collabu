import { createSlice } from "@reduxjs/toolkit";

const userSlice = createSlice({
  name: "user",
  initialState: { username: "", token: "" },
  reducers: {
    setUser: (state, action) => {
      state.username = action.payload.username;
      state.token = action.payload.token;
    },
    logout: (state) => {
      state.username = "";
      state.token = "";
    },
  },
});

export const { setUser, logout } = userSlice.actions;
export default userSlice.reducer;
