-- Run before deploying the settings API when automatic schema updates are disabled.
CREATE TABLE IF NOT EXISTS public.user_settings (
    user_id BIGINT PRIMARY KEY REFERENCES public.users(user_id) ON DELETE CASCADE,
    notification_popups BOOLEAN NOT NULL DEFAULT TRUE,
    activity_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    product_announcements BOOLEAN NOT NULL DEFAULT FALSE
);
