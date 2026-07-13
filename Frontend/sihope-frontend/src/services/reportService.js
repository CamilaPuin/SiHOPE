import api from "./api";
import { getToken } from "../utils/token";

export const citasReport = (desde, hasta) =>
    api
        .get("/api/coordinador/reportes/citas", { params: { desde, hasta } })
        .then((r) => r.data);

/**
 * Descarga el reporte exportado (pdf|excel). Usa fetch directo porque necesitamos el binario
 * como Blob y disparar la descarga en el navegador.
 */
export async function downloadReport(desde, hasta, formato) {
    const base = import.meta.env.VITE_API_URL ?? "";
    const url = `${base}/api/coordinador/reportes/citas/export?desde=${desde}&hasta=${hasta}&formato=${formato}`;
    const res = await fetch(url, {
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    if (!res.ok) {
        throw new Error("No se pudo generar el archivo del reporte.");
    }
    const blob = await res.blob();
    const ext = formato === "excel" ? "xlsx" : "pdf";
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = `reporte-citas-${desde}-a-${hasta}.${ext}`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(link.href);
}
