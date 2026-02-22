public class GreatestTypographicRiver {

   public static String[] textToLines(String texto, int anchoLinea) {
        String[] palabras = texto.split(" ");
        java.util.ArrayList<String> lineas = new java.util.ArrayList<>();
        String lineaActual = "";
        for (String palabra : palabras) {
            // Si es la primera palabra de la línea
            if (lineaActual.isEmpty()) {
                lineaActual = palabra;
            }
            // Si la palabra cabe en la línea actual (considerando el espacio)
            else if (lineaActual.length() + 1 + palabra.length() <= anchoLinea) {
                lineaActual += " " + palabra;
            }
            // Si no cabe, guardamos la línea actual y empezamos una nueva
            else {
                lineas.add(lineaActual);
                lineaActual = palabra;
            }
        }
        // Agregar la última línea si existe
        if (!lineaActual.isEmpty()) {
            lineas.add(lineaActual);
        }
        return lineas.toArray(new String[0]);
    }

    public static String[][] textToMatrix(String texto, int anchoLinea) {
        String[] lineas = textToLines(texto, anchoLinea);
        String[][] matriz = new String[lineas.length][];
        
        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i];
            matriz[i] = new String[linea.length()];
            
            for (int j = 0; j < linea.length(); j++) {
                matriz[i][j] = String.valueOf(linea.charAt(j));
            }
        }
        
        return matriz;
    }
    
    /**
     * Imprime la matriz de forma visual
     */
    public static void printMatrix(String[][] matriz) {
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                if (matriz[i][j].equals(" ")) {
                    System.out.print("*");
                } else {
                    System.out.print(matriz[i][j]);
                }
            }
            System.out.println();
        }
    }

    public static int findLongestRiver(String[][] textMatrix) {
        // Caso base: si la matriz es nula o vacía, no hay ríos
        if (textMatrix == null || textMatrix.length == 0) {
            return 0;
        }
        int numRows = textMatrix.length;
        int maxRiver = 0;
        // dp[i][j] = longitud del río más largo que EMPIEZA en la posición [i][j]
        int[][] dp = new int[numRows][];
        // Inicializar cada fila con el tamaño correcto (las filas pueden tener diferente longitud)
        for (int i = 0; i < numRows; i++) {
            dp[i] = new int[textMatrix[i].length];
        }
        // Caso base: última fila
        // Todos los espacios válidos en la última fila tienen longitud 1
        for (int j = 0; j < textMatrix[numRows - 1].length; j++) {
            // Verificar si es un espacio Y no está al final de la línea
            if (textMatrix[numRows - 1][j].equals(" ") && j < textMatrix[numRows - 1].length - 1) {
                dp[numRows - 1][j] = 1;
                maxRiver = Math.max(maxRiver, 1);
            } else {
                dp[numRows - 1][j] = 0;
            }
        }
        // Llenar tabla DP de abajo hacia arriba (desde la penúltima fila hasta la primera)
        for (int i = numRows - 2; i >= 0; i--) {
            for (int j = 0; j < textMatrix[i].length; j++) {
                // Si NO es un espacio o está al final de la línea, no forma río
                if (!textMatrix[i][j].equals(" ") || j >= textMatrix[i].length - 1) {
                    dp[i][j] = 0;
                    continue;
                }
                // Es un espacio válido, buscar el mejor camino hacia abajo
                int maxNext = 0;
                // Revisar las 3 posiciones posibles en la siguiente fila (izquierda, centro, derecha)
                int[] offsets = {-1, 0, 1};
                // Probar cada desplazamiento posible
                for (int delta : offsets) {
                    int newCol = j + delta; 
                    // Verificar que la nueva columna esté dentro de los límites de la siguiente fila
                    if (newCol >= 0 && newCol < textMatrix[i + 1].length) {
                        // Si hay un río válido en esa posición, actualizar el máximo
                        if (dp[i + 1][newCol] > 0) {
                            maxNext = Math.max(maxNext, dp[i + 1][newCol]);
                        }
                    }
                }
                // La longitud del río desde esta posición es 1 (este espacio) + el mejor siguiente
                dp[i][j] = 1 + maxNext;
                // Actualizar el río más largo encontrado hasta ahora
                maxRiver = Math.max(maxRiver, dp[i][j]);
            }
        }
        return maxRiver;
    }

    /**
     * Encuentra el ancho de línea óptimo y el río más largo para un texto dado.
     * Prueba todos los anchos posibles y retorna el que produce el río más largo.
     * 
     * @param text El texto a analizar
     * @return Array de dos elementos: [ancho óptimo, longitud del río más largo]
     */
    public static int[] findOptimalWidthAndRiver(String text) {
        // 1. Encontrar el ancho mínimo (palabra más larga)
        String[] words = text.split(" ");
        int minWidth = 0;
        for (String word : words) {
            minWidth = Math.max(minWidth, word.length());
        }
        
        // 2. Encontrar el ancho máximo útil
        // Si todo cabe en una línea, no hay río (se necesitan al menos 2 líneas)
        int totalLength = text.length();
        int maxWidth = totalLength - 1;
        
        // Si no hay solución válida
        if (maxWidth < minWidth) {
            return new int[]{minWidth, 0};
        }
        
        int bestWidth = minWidth;
        int bestRiver = 0;
        
        // 3. Probar todos los anchos posibles
        for (int width = minWidth; width <= maxWidth; width++) {
            String[][] matrix = textToMatrix(text, width);
            
            // Validar que la matriz tiene al menos 2 líneas
            if (matrix == null || matrix.length < 2) {
                continue;
            }
            
            int river = findLongestRiver(matrix);
            
            // Si encontramos un río más largo, actualizar
            // (Si hay empate, quedarse con el ancho más pequeño)
            if (river > bestRiver) {
                bestRiver = river;
                bestWidth = width;
            }
        }
        
        return new int[]{bestWidth, bestRiver};
    }
    
    
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
            System.out.println("║  MENÚ DE CASOS DE PRUEBA - V1");
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
        System.out.println("\n");
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║  " + categoryName);
        System.out.println("╚════════════════════════════════════════════════════════╝");
        
        for (int i = 1; i < category.length; i++) {
            String fileName = category[i];
            try {
                System.out.println("\n========================================");
                System.out.println("Probando: " + fileName);
                System.out.println("========================================");
                
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.FileReader(fileName)
                );
                
                String texto = reader.readLine();
                reader.close();
                
                if (texto == null || texto.isEmpty()) {
                    System.out.println("Archivo vacío o no encontrado");
                    continue;
                }
                
                System.out.println("Longitud del texto: " + texto.length() + " caracteres");
                
                // Medir tiempo de ejecución
                long startTime = System.currentTimeMillis();
                
                int[] result = findOptimalWidthAndRiver(texto);
                int optimalWidth = result[0];
                int longestRiver = result[1];
                
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                
                // Mostrar resultados
                System.out.println("Ancho óptimo: " + optimalWidth);
                System.out.println("Río más largo: " + longestRiver);
                System.out.println("Tiempo de ejecución: " + executionTime + " ms");
                
                // Opcional: mostrar la matriz con el ancho óptimo
                if (texto.length() < 200) {
                    System.out.println("\nVisualización con ancho óptimo:");
                    String[][] matriz = textToMatrix(texto, optimalWidth);
                    printMatrix(matriz);
                }
                
            } catch (java.io.FileNotFoundException e) {
                System.out.println("⚠️ Archivo no encontrado: " + fileName);
            } catch (java.io.IOException e) {
                System.out.println("⚠️ Error leyendo archivo: " + fileName);
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("⚠️ Error procesando: " + fileName);
                e.printStackTrace();
            }
        }
    }

}
