import java.util.*;


public class AlgoritmoSJF {
    //colores para la terminal
    public static final String RESET = "\u001B[0m";
    public static final String AMARILLO = "\u001B[33m";
    public static final String VERDE = "\u001B[32m";
    public static final String ROJO = "\u001B[31m";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Proceso> procesos = new ArrayList<>();

        System.out.print("Introduzca el número de procesos: ");
        int numProcesos = scanner.nextInt();
        scanner.nextLine();

        //pedir datos de los procesos
        for (int i = 0; i < numProcesos; i++) {
            System.out.println("\nProceso " + (i + 1));
            System.out.print("Nombre del proceso: ");
            String nombre = scanner.nextLine();

            System.out.print("Tiempo de llegada: ");
            int tiempoLlegada = scanner.nextInt();

            System.out.print("Tiempo de ejecución: ");
            int tiempoEjecucion = scanner.nextInt();
            scanner.nextLine();

            procesos.add(new Proceso(nombre, tiempoLlegada, tiempoEjecucion));
        }

        //ejecutar el SJF
        Map<Integer, List<String>> colaProcesosListos = new HashMap<>();
        Map<Integer, Map<String, Character>> estadoProcesos = ejecutarSJF(procesos, colaProcesosListos);

        //mostrar resultados
        mostrarTabla(procesos);
        mostrarCronogramaDetallado(procesos, estadoProcesos, colaProcesosListos);
        calcularTiemposMedios(procesos);

        scanner.close();
    }

    public static Map<Integer, Map<String, Character>> ejecutarSJF(List<Proceso> procesos, Map<Integer, List<String>> colaProcesosListos) {
        int tiempoActual = 0;
        int procesosCompletados = 0;
        int totalProcesos = procesos.size();

        Map<Integer, Map<String, Character>> estadoProcesos = new HashMap<>();

        //calcula el tiempo máximo posible
        int tiempoMaximoPosible = 0;
        for (Proceso p : procesos) {
            tiempoMaximoPosible += p.tiempoEjecucion; //suma de todos los tiempos de ejecución
        }
        tiempoMaximoPosible *= 2; //margen de seguridad

        // inicializar los estados de los procesos
        for (int t = 0; t <= tiempoMaximoPosible; t++) {
            estadoProcesos.put(t, new HashMap<>());
            for (Proceso p : procesos) {
                estadoProcesos.get(t).put(p.nombre, ' '); // Estado inicial: no llegado
            }
        }

        while (procesosCompletados < totalProcesos) {
            // Asegurarse de que el tiempo actual tiene un mapa inicializado
            if (!estadoProcesos.containsKey(tiempoActual)) {
                estadoProcesos.put(tiempoActual, new HashMap<>());
                for (Proceso p : procesos) {
                    estadoProcesos.get(tiempoActual).put(p.nombre, ' ');
                }
            }

            // Actualizar procesos listos
            List<String> colaActual = new ArrayList<>();
            for (Proceso p : procesos) {
                if (!p.completado && p.tiempoLlegada <= tiempoActual) {
                    estadoProcesos.get(tiempoActual).put(p.nombre, 'L'); // Listo
                    colaActual.add(p.nombre);
                }
            }

            //guarda la cola de procesos listos
            if (!colaActual.isEmpty()) {
                colaProcesosListos.put(tiempoActual, new ArrayList<>(colaActual));
            }
            //busca el proceso con menor tiempo de ejecucion
            Proceso siguienteProceso = null;
            int menorTiempoEjecucion = Integer.MAX_VALUE;

            //busca el proceso con menor tiempo de ejecución entre los listos
            for (Proceso p : procesos) {
                if (!p.completado && p.tiempoLlegada <= tiempoActual && p.tiempoEjecucion < menorTiempoEjecucion) {
                    menorTiempoEjecucion = p.tiempoEjecucion;
                    siguienteProceso = p;
                }
            }

            // Si no hay procesos disponibles, avanzar el tiempo
            if (siguienteProceso == null) {
                tiempoActual++;
                continue;
            }

            //Eejecuta el proceso seleccionado
            siguienteProceso.tiempoComienzo = tiempoActual;
            siguienteProceso.tiempoFinalizacion = tiempoActual + siguienteProceso.tiempoEjecucion;
            siguienteProceso.tiempoRetorno = siguienteProceso.tiempoFinalizacion - siguienteProceso.tiempoLlegada;
            siguienteProceso.tiempoEspera = siguienteProceso.tiempoRetorno - siguienteProceso.tiempoEjecucion;

            //actualizar estados de ejecucion
            for (int t = tiempoActual; t < siguienteProceso.tiempoFinalizacion; t++) {
                //asegurarse de que el tiempo t tiene un mapa inicializado
                if (!estadoProcesos.containsKey(t)) {
                    estadoProcesos.put(t, new HashMap<>());
                    for (Proceso p : procesos) {
                        estadoProcesos.get(t).put(p.nombre, ' ');
                    }
                }

                estadoProcesos.get(t).put(siguienteProceso.nombre, 'E');//Ejecuntado

                //actualiz estado de otros procesos que llegan durante este tiempo
                for (Proceso p : procesos) {
                    if (p != siguienteProceso && !p.completado && p.tiempoLlegada <= t) {
                        estadoProcesos.get(t).put(p.nombre, 'L'); // Listo
                    }
                }

                // Actualizar cola de procesos listos
                List<String> colaSiguiente = new ArrayList<>();
                for (Proceso p : procesos) {
                    if (p != siguienteProceso && !p.completado && p.tiempoLlegada <= t) {
                        colaSiguiente.add(p.nombre);
                    }
                }

                if (!colaSiguiente.isEmpty()) {
                    colaProcesosListos.put(t, colaSiguiente);
                }
            }

            //marcar como finalizado
            //asegurarse de que el tiempo de finalización tiene un mapa inicializado
            if (!estadoProcesos.containsKey(siguienteProceso.tiempoFinalizacion)) {
                estadoProcesos.put(siguienteProceso.tiempoFinalizacion, new HashMap<>());
                for (Proceso p : procesos) {
                    estadoProcesos.get(siguienteProceso.tiempoFinalizacion).put(p.nombre, ' ');
                }
            }

            estadoProcesos.get(siguienteProceso.tiempoFinalizacion).put(siguienteProceso.nombre, 'F');

            tiempoActual = siguienteProceso.tiempoFinalizacion;
            siguienteProceso.completado = true;
            procesosCompletados++;
        }

        return estadoProcesos;
    }

    public static void mostrarTabla(List<Proceso> procesos) {
        System.out.println("\n------ Tabla de Tiempos ------");
        System.out.printf("%-10s %-15s %-15s %-15s %-15s %-15s %-15s%n",
                "Proceso", "T. Llegada", "T. Ejecución", "T. Comienzo",
                "T. Finalización", "T. Retorno", "T. Espera");

        for (Proceso p : procesos) {
            System.out.printf("%-10s %-15d %-15d %-15d %-15d %-15d %-15d%n",
                    p.nombre, p.tiempoLlegada, p.tiempoEjecucion, p.tiempoComienzo,
                    p.tiempoFinalizacion, p.tiempoRetorno, p.tiempoEspera);
        }
    }

    public static void mostrarCronogramaDetallado(List<Proceso> procesos,
                                                  Map<Integer, Map<String, Character>> estadoProcesos,
                                                  Map<Integer, List<String>> colaProcesosListos) {

        //determinar el tiempo máximo
        int tiempoMaximo = 0;
        for (Proceso p : procesos) {
            tiempoMaximo = Math.max(tiempoMaximo, p.tiempoFinalizacion);
        }

        System.out.println("\n------ Cronograma de Procesos ------");

        //encabezado del cornograma
        System.out.print("Tiempo    | ");
        for (int t = 0; t <= tiempoMaximo; t++) {
            System.out.printf("%-3d", t);
        }
        System.out.println();

        System.out.print("----------|");
        for (int t = 0; t <= tiempoMaximo; t++) {
            System.out.print("---");
        }
        System.out.println();

        //imprime estados de los procesos
        for (Proceso p : procesos) {
            System.out.printf("%-10s| ", p.nombre);

            for (int t = 0; t <= tiempoMaximo; t++) {
                char estado = ' ';
                if (estadoProcesos.containsKey(t) && estadoProcesos.get(t).containsKey(p.nombre)) {
                    estado = estadoProcesos.get(t).get(p.nombre);
                }

                switch (estado) {
                    case 'L':
                        System.out.print(AMARILLO + "L  " + RESET);
                        break;
                    case 'E':
                        System.out.print(VERDE + "E  " + RESET);
                        break;
                    case 'F':
                        System.out.print(ROJO + "F  " + RESET);
                        break;
                    default:
                        System.out.print("   ");
                }
            }
            System.out.println();
        }

        // Imprime la cola de procesos listos
        System.out.println("\n------ Cola de Procesos Listos ------");
        for (int t = 0; t <= tiempoMaximo; t++) {
            if (colaProcesosListos.containsKey(t)) {
                System.out.printf("Tiempo %d: Cola = %s\n", t, String.join(", ", colaProcesosListos.get(t)));
            }
        }
    }

    public static void calcularTiemposMedios(List<Proceso> procesos) {
        double tiempoRetornoTotal = 0;
        double tiempoEsperaTotal = 0;

        for (Proceso p : procesos) {
            tiempoRetornoTotal += p.tiempoRetorno;
            tiempoEsperaTotal += p.tiempoEspera;
        }

        double tiempoRetornoMedio = tiempoRetornoTotal / procesos.size();
        double tiempoEsperaMedio = tiempoEsperaTotal / procesos.size();

        System.out.println("\n------ Tiempos Medios ------");
        System.out.printf("Tiempo medio de retorno: %.2f unidades\n", tiempoRetornoMedio);
        System.out.printf("Tiempo medio de espera: %.2f unidades\n", tiempoEsperaMedio);
    }
}
