import React from "react";
import './index.css';
import { createRoot } from 'react-dom/client';
import { Provider } from "react-redux";
import { persistor, store } from "./redux/store";
import App from "./App";
import { PersistGate } from "redux-persist/integration/react";
import { CircularProgress } from "@mui/material";


const root = createRoot(document.getElementById('root'));

root.render(
  <Provider store={store}>
    <PersistGate loading={<CircularProgress />} persistor={persistor}>
      <App />
    </PersistGate>
  </Provider>
);
