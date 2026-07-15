package edu.uptc.swii.sihope.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.uptc.swii.sihope.service.CitaService;

@Component
public class CitaScheduler {

    private static final Logger log = LoggerFactory.getLogger(CitaScheduler.class);

    private final CitaService citaService;

    public CitaScheduler(CitaService citaService) {
        this.citaService = citaService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void sendReminders() {
        int sent = citaService.sendDueReminders();
        if (sent > 0) {
            log.info("Job de recordatorios: se notificaron {} cita(s) próximas.", sent);
        }
    }

    @Scheduled(cron = "0 30 * * * *")
    public void closePastCitas() {
        int closed = citaService.autoMarkAttended();
        if (closed > 0) {
            log.info("Job de cierre: {} cita(s) confirmadas pasaron a ATENDIDA.", closed);
        }
    }
}
