package edu.uptc.swii.sihope.service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.uptc.swii.sihope.domain.Disponibilidad;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.BloqueHorario;
import edu.uptc.swii.sihope.dto.response.MonitorDirectorioResponse;
import edu.uptc.swii.sihope.repository.DisponibilidadRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

/**
 * Lógica de negocio de la disponibilidad horaria del monitor (HU_006).
 *
 * <p>El guardado es un reemplazo total y transaccional: se borran las franjas
 * anteriores y se insertan las nuevas, garantizando que solo queden marcadas las
 * franjas vigentes (criterio de exclusividad de la historia).
 */
@Service
public class DisponibilidadService {

    private final DisponibilidadRepository disponibilidadRepository;
    private final UserRepository userRepository;

    public DisponibilidadService(DisponibilidadRepository disponibilidadRepository,
                                 UserRepository userRepository) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.userRepository = userRepository;
    }

    /** Rol cuyos usuarios aparecen en el directorio de monitores. */
    private static final String ROL_MONITOR = "MONITOR";

    /**
     * Directorio de monitores con su disponibilidad (lectura de HU_006): permite que
     * estudiantes y coordinadores vean las franjas marcadas por cada monitor.
     */
    public List<MonitorDirectorioResponse> listarMonitores() {
        return userRepository.findByRole_NombreOrderByNombresAscApellidosAsc(ROL_MONITOR)
                .stream()
                .map(m -> new MonitorDirectorioResponse(
                        m.getId(),
                        nombreCompleto(m),
                        iniciales(m),
                        m.getCorreo(),
                        consultar(m.getId())))
                .toList();
    }

    private static String nombreCompleto(User u) {
        return ((u.getNombres() == null ? "" : u.getNombres()) + " "
                + (u.getApellidos() == null ? "" : u.getApellidos())).trim();
    }

    /** Iniciales (primera letra de nombre y apellido) para el avatar del directorio. */
    private static String iniciales(User u) {
        char inicialNombre = primeraLetra(u.getNombres());
        char inicialApellido = primeraLetra(u.getApellidos());
        String iniciales = ("" + inicialNombre + inicialApellido).trim();
        return iniciales.isBlank() ? "?" : iniciales.toUpperCase();
    }

    private static char primeraLetra(String texto) {
        return (texto == null || texto.isBlank()) ? ' ' : texto.trim().charAt(0);
    }

    /** Franjas actuales del monitor, ordenadas por día y hora. */
    public List<BloqueHorario> consultar(Integer monitorId) {
        return disponibilidadRepository
                .findByMonitorIdOrderByDiaSemanaAscHoraInicioAsc(monitorId)
                .stream()
                .map(d -> new BloqueHorario(d.getDiaSemana(),
                        d.getHoraInicio().toString(), d.getHoraFin().toString()))
                .toList();
    }

    /**
     * Valida y reemplaza por completo la disponibilidad del monitor.
     *
     * @return lista de errores de validación; si está vacía, los cambios se
     *         persistieron correctamente (nada se guarda si hay errores).
     */
    @Transactional
    public List<String> reemplazar(Integer monitorId, List<BloqueHorario> bloques) {
        List<String> errores = new ArrayList<>();

        User monitor = userRepository.findById(monitorId).orElse(null);
        if (monitor == null) {
            errores.add("El monitor no existe.");
            return errores;
        }

        List<BloqueHorario> lista = (bloques == null) ? List.of() : bloques;
        List<Disponibilidad> aGuardar = new ArrayList<>();

        for (int i = 0; i < lista.size(); i++) {
            BloqueHorario b = lista.get(i);
            String etiqueta = "Bloque " + (i + 1) + ": ";

            if (b.diaSemana() < 1 || b.diaSemana() > 7) {
                errores.add(etiqueta + "el día de la semana debe estar entre 1 (Lunes) y 7 (Domingo).");
                continue;
            }
            LocalTime inicio = parse(b.horaInicio());
            LocalTime fin = parse(b.horaFin());
            if (inicio == null || fin == null) {
                errores.add(etiqueta + "las horas deben tener el formato HH:mm.");
                continue;
            }
            if (!inicio.isBefore(fin)) {
                errores.add(etiqueta + "la hora de inicio debe ser anterior a la de fin.");
                continue;
            }
            aGuardar.add(new Disponibilidad(monitor, b.diaSemana(), inicio, fin));
        }

        errores.addAll(detectarSolapes(aGuardar));

        if (!errores.isEmpty()) {
            return errores;
        }

        disponibilidadRepository.deleteByMonitorId(monitorId);
        disponibilidadRepository.saveAll(aGuardar);
        return errores;
    }

    /** Detecta franjas que se solapan dentro del mismo día. */
    private List<String> detectarSolapes(List<Disponibilidad> bloques) {
        List<String> errores = new ArrayList<>();
        for (int dia = 1; dia <= 7; dia++) {
            final int d = dia;
            List<Disponibilidad> delDia = bloques.stream()
                    .filter(b -> b.getDiaSemana() == d)
                    .sorted(Comparator.comparing(Disponibilidad::getHoraInicio))
                    .toList();
            for (int i = 1; i < delDia.size(); i++) {
                if (delDia.get(i).getHoraInicio().isBefore(delDia.get(i - 1).getHoraFin())) {
                    errores.add("Hay franjas que se solapan en el mismo día. Revisa los horarios.");
                    break;
                }
            }
        }
        return errores;
    }

    private LocalTime parse(String hora) {
        if (hora == null || hora.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(hora.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
