
import com.skillbox.airport.Airport;
import com.skillbox.airport.Flight;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import static java.time.LocalDateTime.*;

public class Main {
    public static void main(String[] args) {

    }

    public static List<Flight> findPlanesLeavingInTheNextTwoHours(Airport airport) {

        LocalDateTime now = now();
        LocalDateTime nowPlus2Hours = now.plusHours(2);

        return airport.getTerminals().stream()
                .flatMap(terminal -> terminal.getFlights().stream())
                .filter(flight -> flight.getType().equals(Flight.Type.DEPARTURE))
                .filter(flight -> flight.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().isAfter(now)
                        && flight.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().isBefore(nowPlus2Hours))
                .collect(Collectors.toList());

        //TODO Метод должден вернуть список рейсов вылетающих в ближайшие два часа.
    }
}