import React, { useEffect } from "react";
import { useSelector, useDispatch } from "react-redux";
import ToastNotification from "../globalComponents/ToastNotification";
import { hideNotification } from "../redux/notificationSlice";

const GlobalNotification = () => {
  const { message, type, visible } = useSelector((state) => state.notification);
  const dispatch = useDispatch();

  useEffect(() => {
    if (visible) {
      const timer = setTimeout(() => {
        dispatch(hideNotification());
      }, 5000);  // Auto-hide after 5 seconds

      return () => clearTimeout(timer);  // Clear timeout on unmount
    }
  }, [visible, dispatch]);

  if (!visible) return null;

  return <ToastNotification message={message} type={type} />;
};

export default GlobalNotification;
