public class GreatestTypographicRiverV2 {

    // ============================================================
    // Convierte el texto en líneas respetando un ancho máximo.
    // No divide palabras.
    // Si una palabra es más larga que el ancho, retorna null.
    // Complejidad: O(n)
    // ============================================================
    public static String[] textToLines(String texto, int anchoLinea) {
        // Separar el texto en palabras usando espacio como delimitador
        String[] palabras = texto.split(" ");
        // Lista dinámica para almacenar las líneas generadas
        java.util.ArrayList<String> lineas = new java.util.ArrayList<>();
        // Línea que se está construyendo actualmente
        String lineaActual = "";
        for (String palabra : palabras) {
            // Si una palabra es más larga que el ancho permitido,
            // no existe una partición válida para este ancho
            if (palabra.length() > anchoLinea) {
                return null;
            }
            // Si la línea está vacía, simplemente agregamos la palabra
            if (lineaActual.isEmpty()) {
                lineaActual = palabra;
            }
            // Si la palabra cabe (considerando un espacio extra entre palabras)
            else if (lineaActual.length() + 1 + palabra.length() <= anchoLinea) {
                lineaActual += " " + palabra;
            }
            // Si no cabe, cerramos la línea actual y comenzamos una nueva
            else {
                lineas.add(lineaActual);
                lineaActual = palabra;
            }
        }
        // Agregar la última línea si no quedó vacía
        if (!lineaActual.isEmpty()) {
            lineas.add(lineaActual);
        }
        // Convertir ArrayList a arreglo y retornarlo
        return lineas.toArray(new String[0]);
    }

    // ============================================================
    // Calcula el río tipográfico más largo usando Programación
    // Dinámica directamente sobre String[] (sin matriz 2D).
    //
    // dp[i][j] = longitud del río que empieza en línea i,
    // columna j.
    //
    // Se permite moverse hacia abajo:
    // misma columna, izquierda o derecha (±1).
    //
    // Complejidad: O(n)
    // ============================================================
    public static int findLongestRiverOptimized(String[] lines) {
        // Caso base: texto vacío o nulo
        if (lines == null || lines.length == 0) {
            return 0;
        }
        int numRows = lines.length;
        int maxRiver = 0;
        // Crear estructura DP donde cada fila puede tener diferente tamaño
        int[][] dp = new int[numRows][];
        for (int i = 0; i < numRows; i++) {
            dp[i] = new int[lines[i].length()];
        }
        // ============================
        // Caso base: última fila
        // ============================
        // En la última fila, un espacio válido aporta longitud 1
        for (int j = 0; j < lines[numRows - 1].length(); j++) {
            // Solo cuentan espacios que NO estén al final de línea
            if (lines[numRows - 1].charAt(j) == ' '
                    && j < lines[numRows - 1].length() - 1) {
                dp[numRows - 1][j] = 1;
                maxRiver = 1;
            } else {
                dp[numRows - 1][j] = 0;
            }
        }
        // ============================
        // Llenar DP de abajo hacia arriba
        // ============================
        for (int i = numRows - 2; i >= 0; i--) {
            for (int j = 0; j < lines[i].length(); j++) {
                // Si no es espacio o es espacio final,
                // no puede iniciar un río
                if (lines[i].charAt(j) != ' '
                        || j >= lines[i].length() - 1) {
                    dp[i][j] = 0;
                    continue;
                }
                int maxNext = 0;
                // Revisar las tres posiciones posibles en la fila inferior
                // (izquierda, misma columna, derecha)
                int[] offsets = {-1, 0, 1};
                for (int delta : offsets) {
                    // Nueva columna candidata en la fila inferior
                    // representa movimiento -1, 0 o +1
                    int newCol = j + delta;
                    // Verificar que la columna esté dentro de límites
                    if (newCol >= 0
                            && newCol < lines[i + 1].length()) {

                        maxNext = Math.max(maxNext,
                                dp[i + 1][newCol]);
                    }
                }
                // Longitud del río desde esta posición
                // = 1 (espacio actual) + mejor continuación abajo
                dp[i][j] = 1 + maxNext;

                // Actualizar máximo global encontrado
                maxRiver = Math.max(maxRiver, dp[i][j]);
            }
        }
        return maxRiver;
    }

    // ============================================================
    // Busca el ancho óptimo que maximiza el río más largo.
    //
    // Explora todos los anchos posibles desde:
    // - minWidth = palabra más larga
    // - maxWidth = min(longitud total - 1, 500)
    //
    // Aplica optimizaciones:
    // 1. Saltar configuraciones imposibles
    // 2. Saltar si no puede mejorar el mejor resultado
    // 3. Early stopping si se alcanza el máximo teórico
    // ============================================================
    public static int[] findOptimalWidthAndRiver(String text) {
        // Calcular ancho mínimo posible (la palabra más larga)
        String[] words = text.split(" ");
        int minWidth = 0;
        for (String word : words) {
            minWidth = Math.max(minWidth, word.length());
        }
        // Definir ancho máximo razonable
        // Se limita a 500 para evitar explorar anchos excesivamente grandes
        int totalLength = text.length();
        int maxWidth = Math.min(totalLength - 1, 500);
        // Si el ancho máximo es menor que el mínimo posible,
        // no existe configuración válida
        if (maxWidth < minWidth) {
            return new int[]{minWidth, 0};
        }
        // Inicialmente asumimos que el mejor ancho es el mínimo
        int bestWidth = minWidth;
        // Longitud máxima de río encontrada hasta el momento
        int bestRiver = 0;
        // Probar todos los anchos posibles
        for (int width = minWidth; width <= maxWidth; width++) {
            String[] lines = textToLines(text, width);
            // Si la partición no es válida o hay menos de 2 líneas,
            // no puede existir río
            if (lines == null || lines.length < 2) {
                continue;
            }
            // Optimización importante:
            // Un río no puede ser más largo que el número total de líneas,
            // porque cada línea solo puede aportar 1 unidad de longitud.
            // Si el número de líneas es menor que el mejor río ya encontrado,
            // esta configuración jamás podrá superarlo.
            if (lines.length < bestRiver) {
                continue;
            }
            int river = findLongestRiverOptimized(lines);
            // Actualizar mejor solución encontrada
            if (river > bestRiver) {
                bestRiver = river;
                bestWidth = width;
            }
            // Early stopping:
            // Si el río ocupa todas las líneas disponibles,
            // se alcanzó el máximo teórico posible
            if (river == lines.length) {
                break;
            }
        }
        return new int[]{bestWidth, bestRiver};
    }

    // ============================================================
    // Método principal para pruebas
    // Permite elegir qué categoría de casos probar
    // ============================================================
    public static void main(String[] args) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        
        String[][] testCategories = {
            {"Casos básicos", "inputSmall.txt", "inputSmall2.txt", "inputSmall3.txt"},
            {"CasosDePruebaA-Z", 
            "CasosDePruebaA-Z/a-z500.txt",
            "CasosDePruebaA-Z/a-z1000.txt",
            "CasosDePruebaA-Z/a-z20000.txt",
            "CasosDePruebaA-Z/a-z50000.txt",
            "CasosDePruebaA-Z/a-z100000.txt"
            },
            {"CasosDePruebaCicero",
            "CasosDePruebaCicero/cicero500.txt",
            "CasosDePruebaCicero/cicero1000.txt",
            "CasosDePruebaCicero/cicero20000.txt",
            "CasosDePruebaCicero/cicero50000.txt",
            "CasosDePruebaCicero/cicero100000.txt"
            },
            {"CasosDePruebaLiLanguages",
            "CasosDePruebaLiLanguages/LiLang500.txt",
            "CasosDePruebaLiLanguages/LiLang1000.txt",
            "CasosDePruebaLiLanguages/LiLang20000.txt",
            "CasosDePruebaLiLanguages/LiLang50000.txt",
            "CasosDePruebaLiLanguages/LiLang100000.txt"
            },
            {"CasosDePruebaLoremIpsum",
            "CasosDePruebaLoremIpsum/loremipsum500.txt",
            "CasosDePruebaLoremIpsum/loremipsum1000.txt",
            "CasosDePruebaLoremIpsum/loremipsum20000.txt",
            "CasosDePruebaLoremIpsum/loremipsum50000.txt",
            "CasosDePruebaLoremIpsum/loremipsum100000.txt"
            }
        };
        
        while (true) {
            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║  MENÚ DE CASOS DE PRUEBA");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            System.out.println("\n0. Probar TODOS los casos");
            for (int i = 0; i < testCategories.length; i++) {
                System.out.println((i + 1) + ". " + testCategories[i][0]);
            }
            System.out.println((testCategories.length + 1) + ". Salir");
            
            System.out.print("\nSeleccione una opción: ");
            int opcion = scanner.nextInt();
            
            if (opcion == testCategories.length + 1) {
                System.out.println("\n¡Hasta luego!");
                break;
            }
            
            if (opcion == 0) {
                // Probar todos
                for (String[] category : testCategories) {
                    probarCategoria(category);
                }
            } else if (opcion >= 1 && opcion <= testCategories.length) {
                // Probar categoría específica
                probarCategoria(testCategories[opcion - 1]);
            } else {
                System.out.println("⚠️ Opción inválida");
            }
        }
        
        scanner.close();
    }

    // ============================================================
    // Método auxiliar para probar una categoría
    // ============================================================
    private static void probarCategoria(String[] category) {
        String categoryName = category[0];
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║  " + categoryName);
        System.out.println("╚════════════════════════════════════════════════════════╝");
        
        for (int i = 1; i < category.length; i++) {
            String fileName = category[i];
            try {
                System.out.println("\n--- " + fileName + " ---");
                
                // Leer archivo de entrada
                java.io.BufferedReader reader =
                        new java.io.BufferedReader(
                                new java.io.FileReader(fileName));
                String texto = reader.readLine();
                reader.close();
                
                if (texto == null || texto.isEmpty()) {
                    System.out.println("Archivo vacío o no encontrado");
                    continue;
                }
                
                long startTime = System.currentTimeMillis();
                // Ejecutar búsqueda del ancho óptimo
                int[] result = findOptimalWidthAndRiver(texto);
                long endTime = System.currentTimeMillis();
                
                System.out.println("Longitud: " + texto.length() + " chars | " +
                                "Ancho: " + result[0] + " | " +
                                "Río: " + result[1] + " | " +
                                "Tiempo: " + (endTime - startTime) + " ms");
                
            } catch (java.io.FileNotFoundException e) {
                System.out.println("⚠️ Archivo no encontrado: " + fileName);
            } catch (Exception e) {
                System.out.println("⚠️ Error: " + fileName);
                e.printStackTrace();
            }
        }
    }
}
