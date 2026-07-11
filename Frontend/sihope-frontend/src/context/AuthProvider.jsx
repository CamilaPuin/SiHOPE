import { useCallback, useEffect, useState } from "react";
import { AuthContext } from "./authContext";
import * as authService from "../services/authService";
import { getToken } from "../utils/token";

export default function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    // Only start in the loading state when there is a token to validate;
    // otherwise loading is already resolved and the effect below is a no-op.
    const [loading, setLoading] = useState(() => Boolean(getToken()));

    useEffect(() => {
        if (!getToken()) return undefined;
        let active = true;
        authService
            .me()
            .then((res) => {
                if (active) setUser(res.data ?? null);
            })
            .catch(() => {
                if (active) setUser(null);
            })
            .finally(() => {
                if (active) setLoading(false);
            });
        return () => {
            active = false;
        };
    }, []);

    const signIn = useCallback(async (credentials) => {
        const res = await authService.login(credentials);
        const sessionUser = res.data?.usuario ?? null;
        setUser(sessionUser);
        return sessionUser;
    }, []);

    const signOut = useCallback(async () => {
        try {
            await authService.logout();
        } finally {
            setUser(null);
        }
    }, []);

    const value = {
        user,
        usuario: user,
        loading,
        cargando: loading,
        signIn,
        iniciarSesion: signIn,
        signOut,
        cerrarSesion: signOut,
        isAuthenticated: Boolean(user),
        estaAutenticado: Boolean(user)
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
