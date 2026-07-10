import { useCallback, useEffect, useState } from "react";
import { AuthContext } from "./authContext";
import * as authService from "../services/authService";
import { obtenerToken } from "../utils/token";

/**
 * Provee el estado de sesión a toda la app. Al montar, si hay un token JWT en
 * localStorage, hidrata la sesión con GET /api/auth/me. Mientras resuelve,
 * `cargando` es true para que las rutas protegidas no redirijan antes de tiempo.
 */
export default function AuthProvider({ children }) {
    const [usuario, setUsuario] = useState(null);
    const [cargando, setCargando] = useState(true);

    useEffect(() => {
        let activo = true;
        if (!obtenerToken()) {
            setCargando(false);
            return;
        }
        authService
            .me()
            .then((res) => {
                if (activo) setUsuario(res.data ?? null);
            })
            .catch(() => {
                // Token inválido/expirado o error de red → no hay usuario autenticado.
                if (activo) setUsuario(null);
            })
            .finally(() => {
                if (activo) setCargando(false);
            });
        return () => {
            activo = false;
        };
    }, []);

    const iniciarSesion = useCallback(async (credenciales) => {
        const res = await authService.login(credenciales);
        const usuarioSesion = res.data?.usuario ?? null;
        setUsuario(usuarioSesion);
        return usuarioSesion;
    }, []);

    const cerrarSesion = useCallback(async () => {
        try {
            await authService.logout();
        } finally {
            setUsuario(null);
        }
    }, []);

    const value = {
        usuario,
        cargando,
        iniciarSesion,
        cerrarSesion,
        estaAutenticado: Boolean(usuario)
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
