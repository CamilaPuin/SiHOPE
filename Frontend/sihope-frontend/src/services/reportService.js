import api from "./api";
import { getToken, clearToken } from "../utils/token";

export const citasReport = (desde, hasta, monitorId) =>
    api
        .get("/api/coordinador/reportes/citas", {
            params: monitorId ? { desde, hasta, monitorId } : { desde, hasta }
        })
        .then((r) => r.data);

/**
 * Descarga el reporte exportado (pdf|excel). Usa fetch directo porque necesitamos el binario
 * como Blob y disparar la descarga en el navegador.
 *
 * Si el backend responde con error (401/403/400/500) el cuerpo llega como JSON
 * ({success, message, data}); en ese caso NO lo tratamos como archivo: leemos el
 * mensaje real y lo lanzamos para que la UI lo muestre.
 */
export async function downloadReport(desde, hasta, formato, monitorId) {
    const base = import.meta.env.VITE_API_URL ?? "";
    const params = new URLSearchParams({ desde, hasta, formato });
    if (monitorId) {
        params.set("monitorId", monitorId);
    }
    const url = `${base}/api/coordinador/reportes/citas/export?${params.toString()}`;

    let res;
    try {
        res = await fetch(url, {
            headers: { Authorization: `Bearer ${getToken()}` }
        });
    } catch {
        throw new Error(
            "No se pudo conectar con el servidor. Verifica tu conexión e inténtalo de nuevo."
        );
    }

    if (!res.ok) {
        if (res.status === 401) {
            clearToken();
        }
        throw new Error(await errorMessage(res));
    }

    const blob = await res.blob();

    // Defensa extra: si el servidor respondió 200 pero devolvió JSON en vez del binario,
    // no descargamos un archivo corrupto: mostramos el mensaje.
    const type = blob.type || "";
    if (type.includes("application/json")) {
        const text = await blob.text();
        throw new Error(extractMessage(text) ?? "No se pudo generar el archivo del reporte.");
    }

    const ext = formato === "excel" ? "xlsx" : "pdf";
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = `reporte-citas-${desde}-a-${hasta}.${ext}`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(link.href);
}

async function errorMessage(res) {
    try {
        const text = await res.text();
        return (
            extractMessage(text) ??
            (res.status === 401 || res.status === 403
                ? "Tu sesión no es válida o no tienes permisos. Inicia sesión de nuevo."
                : "No se pudo generar el archivo del reporte.")
        );
    } catch {
        return "No se pudo generar el archivo del reporte.";
    }
}

function extractMessage(text) {
    if (!text) return null;
    try {
        const body = JSON.parse(text);
        return body?.message ?? null;
    } catch {
        return null;
    }
}
