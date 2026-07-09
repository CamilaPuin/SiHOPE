import { useContext } from "react";
import { AuthContext } from "../context/authContext";

/** Acceso al estado de autenticación. Debe usarse dentro de <AuthProvider>. */
export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) {
        throw new Error("useAuth debe usarse dentro de <AuthProvider>.");
    }
    return ctx;
}
