import { configureStore } from "@reduxjs/toolkit";
import userReducer from "./userSlice";
import notificationReducer from "./notificationSlice";
import repositoryStructureReducer from "./repositoryStructureSlice";
import { persistStore, persistReducer } from 'redux-persist';
import webSocketMiddleware from "./webSocketMiddleware";
import localforage from "localforage";
import navigationReducer from "./navigationSlice";

// Persist configuration for user and repository structure
const userPersistConfig = {
  key: 'user',
  storage: localforage,
  whitelist: ['token', 'username'], // persist token and username
  timeout: 10000,
};

// const repositoryPersistConfig = {
//   key: 'repositoryStructure',
//   storage: localforage,
//   whitelist: ['repositoryStructure'], // persist repository structure
//   timeout: 10000,
// };

// Create persisted reducers
const persistedUserReducer = persistReducer(userPersistConfig, userReducer);
// const persistedRepositoryReducer = persistReducer(repositoryPersistConfig, repositoryStructureReducer);

// Create store with persisted reducers
export const store = configureStore({
  reducer: {
    user: persistedUserReducer,
    notification: notificationReducer,
    repositoryStructure: repositoryStructureReducer,
    navigation: navigationReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: [
          "persist/PERSIST",
          "persist/REHYDRATE",
        ], // Ignore redux-persist actions
      },
    }).concat(webSocketMiddleware),
});

// Persistor to manage persistence
export const persistor = persistStore(store);
