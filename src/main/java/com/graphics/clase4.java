package com.graphics; // Reúne la versión final con las otras tres lecciones.

import static org.lwjgl.glfw.GLFW.*; // Permite consultar la tecla que controla el minimapa.
import static org.lwjgl.opengl.GL33.*; // Permite cambiar viewport, recorte y buffers de dibujo.

public class clase4 extends clase3 {

    // ==================== 1. ESTADO DEL JUEGO ====================
    private final PanelInfo panel = new PanelInfo();
    private final PanelInfo cartelVictoria = new PanelInfo(1);
    private final PanelInfo botonFreno = new PanelInfo(2);
    private final PanelInfo menuTitulo = new PanelInfo();
    private final PanelInfo[] botonesMenu = {new PanelInfo(3), new PanelInfo(3), new PanelInfo(3)};
    private final PanelInfo navegador = new PanelInfo(4);
    protected int choques;
    private boolean contactoContado;
    private float tiempoSinContacto;
    private final PanelInfo aviso = new PanelInfo(3);
    private final SonidoJuego sonido = new SonidoJuego();
    protected boolean enInicio;
    protected boolean silenciado;
    protected float avisoEntrega;
    protected float avisoChoque;
    private float relojVisual;
    protected boolean frenoPorBoton = false;
    private double cursorX;
    private double cursorY;
    protected final Cruce[] cruces = crearCruces();
    protected final VehiculoTrafico[] trafico = {
        new VehiculoTrafico(50, 30, 0.18f, 0.55f, 0.95f),
        new VehiculoTrafico(30, 65, 0.90f, 0.70f, 0.12f),
        new VehiculoTrafico(10, 20, 0.35f, 0.80f, 0.38f)
    };
    protected float tiempoUrbano = 0;
    protected boolean pausa = false;
    private boolean mostrarAyuda = true;
    private boolean mostrarMapa = true; // Muestra el minimapa desde el inicio.
    protected int entregas = 0; // Cuenta las entregas completadas; también identifica el siguiente destino.
    protected float tiempo = 0; // Acumula los segundos de la partida hasta completar el recorrido.
    protected static final float[][] DESTINOS = { // Cada fila contiene X y Z de una parada sobre la calle.
        {50, -50},
        {-50, -50},
        {-50, 50}
    };

    private static Cruce[] crearCruces() {
        java.util.List<Cruce> resultado = new java.util.ArrayList<>();
        for (int fila = 0; fila < MAPA.length; fila += 2) {
            for (int columna = 0; columna < MAPA[fila].length; columna += 2) {
                resultado.add(new Cruce(centro(columna), centro(fila)));
            }
        }
        return resultado.toArray(new Cruce[0]);
    }

    @Override
    protected void iniciarExtras() {
        panel.iniciar();
        cartelVictoria.iniciar();
        botonFreno.iniciar();
        menuTitulo.iniciar();
        aviso.iniciar();
        navegador.iniciar();
        for (PanelInfo boton : botonesMenu) boton.iniciar();
        enInicio = true;
        sonido.iniciar();
    }

    @Override
    protected void limpiarExtras() {
        sonido.limpiar();
        menuTitulo.limpiar();
        aviso.limpiar();
        navegador.limpiar();
        for (PanelInfo boton : botonesMenu) boton.limpiar();
        panel.limpiar();
        cartelVictoria.limpiar();
        botonFreno.limpiar();
    }

    // ==================== 2. CONTROLES Y REINICIO ====================

    /** Añade el interruptor del minimapa a los controles anteriores. */
    @Override // Amplía las teclas definidas por clase3.
    protected void tecla(int key) {
        super.tecla(key); // Conserva salida, cámara, reinicio y luces.
        if (key == GLFW_KEY_H) {
            mostrarAyuda = !mostrarAyuda;
        }
        if (key == GLFW_KEY_ENTER) {
            enInicio = false;
            pausa = false;
            frenoPorBoton = false;
        }
        if (key == GLFW_KEY_V) silenciado = !silenciado;
        if (key == GLFW_KEY_P && !enInicio) {
            pausa = !pausa;
            frenoPorBoton = false;
        }
        if (key == GLFW_KEY_M) { // Comprueba si se pulsó la tecla del mapa.
            mostrarMapa = !mostrarMapa; // Alterna entre mostrar y ocultar la vista superior.
        }
    }

    /** Reinicia el vehículo y el progreso de las entregas. */
    @Override // Amplía el reinicio definido en clase2.
    protected void reiniciar() {
        super.reiniciar(); // Restaura posición, velocidad y orientación del vehículo.
        entregas = 0; // Vuelve a seleccionar la primera parada.
        tiempo = 0; // Reinicia el cronómetro de la partida.
        tiempoUrbano = 0;
        pausa = false;
        frenoPorBoton = false;
        avisoEntrega = 0;
        avisoChoque = 0;
        choques = 0;
        contactoContado = false;
        contactoActual = false;
        tiempoSinContacto = 0;
        sacudidaCamara = 0;
        for (Cruce cruce : cruces) {
            cruce.reiniciar();
        }
        for (VehiculoTrafico vehiculo : trafico) {
            vehiculo.reiniciar();
        }
    }

    // ==================== 3. REGLAS DE LAS ENTREGAS ====================

    /** Actualiza el vehículo y comprueba si llegó y frenó en el destino activo. */
    @Override // Añade el objetivo del juego al movimiento heredado.
    protected void actualizar(float deltaTime) {
        sonido.actualizar(velocidad, frenando, enInicio || pausa || tieneVictoria(), silenciado);
        if (pausa || enInicio) {
            actualizarVista(deltaTime);
            actualizarTitulo();
            return;
        }
        relojVisual += deltaTime;
        avisoEntrega = Math.max(0, avisoEntrega - deltaTime);
        avisoChoque = Math.max(0, avisoChoque - deltaTime);
        sacudidaCamara = avisoChoque * 0.32f * (float) Math.sin(relojVisual * 85);
        super.actualizar(deltaTime);
        actualizarContacto(deltaTime);
        tiempoUrbano += deltaTime;
        actualizarTrafico(deltaTime);
        actualizarEntregas(deltaTime);
        actualizarTitulo();
    }

    protected void actualizarEntregas(float deltaTime) {
        if (entregas >= DESTINOS.length) {
            return;
        }
        tiempo += deltaTime;
        float distanciaX = autoX - DESTINOS[entregas][0];
        float distanciaZ = autoZ - DESTINOS[entregas][1];
        boolean estaCerca = distanciaX * distanciaX + distanciaZ * distanciaZ < 9;
        boolean estaFrenando = Math.abs(velocidad) < 1;
        if (estaCerca && estaFrenando) {
            entregas++;
            avisoEntrega = 2.5f;
            sonido.reproducir(false, tieneVictoria());
        }
    }

    @Override
    protected void alChocar(float intensidad) {
        if (intensidad > 1 && !contactoContado) {
            contactoContado = true;
            tiempoSinContacto = 0;
            if (!tieneVictoria()) choques++;
            avisoChoque = 0.35f;
            sonido.reproducir(true, false);
        }
    }

    protected void actualizarContacto(float deltaTime) {
        if (contactoActual) tiempoSinContacto = 0;
        else {
            tiempoSinContacto += deltaTime;
            if (tiempoSinContacto >= 0.25f) contactoContado = false;
        }
    }

    protected float rumboDestino() {
        if (tieneVictoria()) return 0;
        float dx = DESTINOS[entregas][0] - autoX;
        float dz = DESTINOS[entregas][1] - autoZ;
        if (dx * dx + dz * dz < 0.01f) return 0;
        float diferencia = (float) Math.atan2(-dx, -dz) - angulo;
        return (float) Math.atan2(Math.sin(diferencia), Math.cos(diferencia));
    }

    protected String textoRumbo() {
        float rumbo = rumboDestino();
        if (Math.abs(rumbo) > Math.PI * 0.75) return "Detras de ti";
        if (Math.abs(rumbo) < Math.PI / 8) return "Sigue adelante";
        return rumbo > 0 ? "A tu izquierda" : "A tu derecha";
    }

    protected String resumenFinal() {
        return "YOU WIN !\nTiempo total: " + (int) tiempo + " s\nEntregas: " + entregas + "/" + DESTINOS.length
             + "\nChoques: " + choques + "\nR para jugar otra vez";
    }

    protected void accionMenu(int indice) {
        if (indice == 0) { enInicio = false; pausa = false; }
        if (indice == 1) { reiniciar(); enInicio = false; }
        if (indice == 2) silenciado = !silenciado;
        frenoPorBoton = false;
    }

    protected void actualizarTrafico(float deltaTime) {
        for (VehiculoTrafico vehiculo : trafico) {
            vehiculo.actualizar(deltaTime, tiempoUrbano, cruces, trafico, autoX, autoZ, this, chata.obstaculos());
        }
    }

    protected boolean tieneVictoria() {
        return entregas >= DESTINOS.length;
    }

    @Override
    protected boolean frenoSolicitado() {
        return frenoPorBoton || super.frenoSolicitado();
    }

    @Override
    protected void moverRaton(double x, double y) {
        super.moverRaton(x, y);
        cursorX = x;
        cursorY = y;
    }

    @Override
    protected void botonRaton(int boton, int accion) {
        super.botonRaton(boton, accion);
        if (boton != GLFW_MOUSE_BUTTON_LEFT) {
            return;
        }
        if (accion == GLFW_RELEASE) {
            frenoPorBoton = false;
            return;
        }
        if (accion == GLFW_PRESS) {
            int[] w = {ancho};
            int[] h = {alto};
            if (ventana != 0) {
                double[] x = new double[1];
                double[] y = new double[1];
                glfwGetCursorPos(ventana, x, y);
                cursorX = x[0];
                cursorY = y[0];
                glfwGetWindowSize(ventana, w, h);
            }
            if (w[0] > 0 && h[0] > 0) {
                float x = (float) (cursorX * ancho / w[0]);
                float y = alto - (float) (cursorY * alto / h[0]);
                if (enInicio || pausa) {
                    for (int i = 0; i < botonesMenu.length; i++) {
                        int[] botonMenu = PanelInfo.rectMenu(ancho, alto, i);
                        if (x >= botonMenu[0] && x <= botonMenu[0] + botonMenu[2]
                            && y >= botonMenu[1] && y <= botonMenu[1] + botonMenu[3]) accionMenu(i);
                    }
                    return;
                }
                int[] r = PanelInfo.rectFreno(ancho, alto);
                frenoPorBoton = x >= r[0] && x <= r[0] + r[2] && y >= r[1] && y <= r[1] + r[3];
            }
        }
    }

    @Override
    protected boolean puedeCircular(float x, float z) {
        if (!super.puedeCircular(x, z)) {
            return false;
        }
        if (trafico != null) {
            for (VehiculoTrafico vehiculo : trafico) {
                if (Colisiones.autos(x, z, angulo, vehiculo.x, vehiculo.z, vehiculo.angulo)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected boolean puedeCircularChata(float x, float z, float radio) {
        if (!super.puedeCircularChata(x, z, radio)) return false;
        if (trafico != null) {
            for (VehiculoTrafico otro : trafico) {
                if (Colisiones.circuloAuto(x, z, radio, otro.x, otro.z, otro.angulo)) return false;
            }
        }
        return true;
    }

    /** Compone el progreso que se añade al título de la ventana. */
    @Override // Amplía los indicadores de día/noche y faros de clase3.
    protected String estadoExtra() {
        String mensaje = super.estadoExtra(); // Recupera los indicadores de iluminación.
        mensaje += " | M: mapa | "; // Muestra la tecla que alterna el minimapa.
        if (entregas == DESTINOS.length) { // Selecciona el texto de victoria al completar las tres paradas.
            mensaje += "YOU WIN ! en " + (int) tiempo + " s | R: jugar otra vez";
        } else { // Durante el recorrido muestra progreso e instrucciones.
            mensaje += "Entregas " + entregas + "/" + DESTINOS.length; // Indica cuántas paradas se completaron.
            mensaje += " | Frena en la marca dorada | " + (int) tiempo + " s"; // Explica la condición de entrega y el tiempo.
        }
        if (pausa) {
            mensaje += " | PAUSA";
        }
        return mensaje; // Entrega el texto a actualizarTitulo() de clase2.
    }

    // ==================== 4. ESCENA FINAL Y DESTINO ====================

    /** Añade decoración y señal del destino a la escena iluminada. */
    @Override // Amplía el dibujo acumulado de las tres etapas anteriores.
    protected void escena() {
        super.escena(); // Dibuja la ciudad, el auto y las farolas.
        for (VehiculoTrafico vehiculo : trafico) {
            dibujarVehiculo(vehiculo.x, vehiculo.z, vehiculo.angulo, vehiculo.rojo, vehiculo.verde, vehiculo.azul);
        }
        if (!vistaMapa) { // Los detalles pequeños solo son necesarios en la vista principal.
            decorarCiudad(); // Añade árboles, bancos, ventanas y señalización urbana.
            dibujarCruces();
            dibujarSenales();
        }
        if (entregas < DESTINOS.length) { // Dibuja un objetivo únicamente mientras queden entregas.
            dibujarDestino(); // Coloca la marca dorada en la parada activa.
        }
    }

    /** Marca la próxima parada con una plataforma y una baliza flotante. */
    private void dibujarDestino() {
        float x = DESTINOS[entregas][0]; // Lee el X de la próxima entrega.
        float z = DESTINOS[entregas][1]; // Lee el Z de la próxima entrega.
        entero("uEmision", 1); // Hace que el objetivo sea visible incluso de noche.
        if (vistaMapa) caja(x, 0.06f, z, 5, 0.08f, 5, 1, 0.72f, 0.12f);
        else {
            float radio = 2.5f + 0.12f * (float) Math.sin(relojVisual * 3);
            for (int i = 0; i < 48; i++) {
                float a = i * (float) Math.PI * 2 / 48;
                cajaGirada(x + (float) Math.sin(a) * radio, 0.06f, z + (float) Math.cos(a) * radio,
                    0.38f, 0.07f, 0.16f, 1, 0.72f, 0.12f, a);
            }
        } // Dibuja una marca dorada sobre el asfalto.
        if (!vistaMapa) { // Evita añadir una baliza tridimensional al mapa pequeño.
            float alturaBaliza = 3.5f + (float) Math.sin(tiempo * 2) * 0.3f; // Hace oscilar la baliza suavemente.
            caja(x, alturaBaliza + 0.5f, z, 0.18f, 0.8f, 0.18f, 1, 0.8f, 0.15f);
            malla(tejado, x, alturaBaliza, z, 0.9f, 0.6f, 0.3f, 1, 0.8f, 0.15f,
                  relojVisual, 0, (float) Math.PI); // Dibuja el cubo giratorio del objetivo.
        }
        entero("uEmision", 0); // Devuelve a los siguientes objetos su iluminación normal.
    }

    // ==================== 5. DECORACIÓN DE LAS MANZANAS ====================

    /** Decide qué decoración corresponde a cada tipo de parcela. */
    private void decorarCiudad() {
        for (int fila = 0; fila < MAPA.length; fila++) { // Recorre las filas del mapa.
            for (int columna = 0; columna < MAPA[fila].length; columna++) { // Recorre las columnas de esa fila.
                float x = centro(columna); // Obtiene el centro horizontal de la parcela.
                float z = centro(fila); // Obtiene el centro de la parcela en profundidad.
                int tipo = MAPA[fila][columna]; // Lee el contenido de la celda.
                if (tipo == 2) { // Detecta una parcela de parque.
                    dibujarParque(x, z); // Añade árboles y un banco.
                }

                if (tipo != 0) { // La señalización se coloca junto a las manzanas, no en celdas de calle.
                    dibujarPasoPeatonal(x, z); // Añade el cruce pintado sobre la calle contigua.

                }
            }
        }
    }

    /** Construye cuatro árboles y un banco utilizando cajas. */
    private void dibujarParque(float x, float z) {
        float[] posiciones = {-2.5f, 2.5f}; // Define desplazamientos respecto al centro de la parcela.
        for (float desplazamientoX : posiciones) { // Selecciona el lado izquierdo o derecho del parque.
            for (float desplazamientoZ : posiciones) { // Selecciona el lado delantero o trasero.
                float arbolX = x + desplazamientoX; // Convierte el desplazamiento local en coordenada X mundial.
                float arbolZ = z + desplazamientoZ; // Convierte el desplazamiento local en coordenada Z mundial.
                caja(arbolX, 1.2f, arbolZ, 0.35f, 2, 0.35f, 0.38f, 0.22f, 0.12f); // Dibuja el tronco marrón.
                caja(arbolX, 2.7f, arbolZ, 2, 2.3f, 2, 0.12f, 0.42f, 0.23f); // Dibuja la copa verde del árbol.
            }
        }
        caja(x, 0.65f, z, 3, 0.25f, 0.8f, 0.55f, 0.30f, 0.13f); // Dibuja el asiento de madera del banco.
        caja(x, 0.4f, z, 2, 0.6f, 0.35f, 0.22f, 0.24f, 0.24f); // Dibuja el soporte oscuro del banco.
        caja(x, 1, z + 0.35f, 3, 0.7f, 0.15f, 0.55f, 0.30f, 0.13f); // Dibuja el respaldo detrás del asiento.
    }

    /** Dibuja las franjas blancas sobre la calle contigua al norte de la manzana. */
    private void dibujarPasoPeatonal(float x, float z) {
        for (int desplazamiento = -4; desplazamiento <= 4; desplazamiento++) {
            caja(x - 4.5f, 0.045f, z - 10 + desplazamiento, 1.4f, 0.04f, 0.45f,
                 0.85f, 0.87f, 0.83f);
        }
    }

    private void dibujarCruces() {
        for (Cruce cruce : cruces) {
            dibujarSemaforo(cruce, -6, 6, (float) Math.PI / 2, true);
            dibujarSemaforo(cruce, 6, -6, -(float) Math.PI / 2, true);
            dibujarSemaforo(cruce, -6, -6, 0, false);
            dibujarSemaforo(cruce, 6, 6, (float) Math.PI, false);
        }
    }

    private void dibujarSemaforo(Cruce cruce, float dx, float dz, float giro, boolean horizontal) {
        float x = cruce.x + dx;
        float z = cruce.z + dz;
        if (Math.abs(x) > LIMITE || Math.abs(z) > LIMITE) {
            return;
        }
        caja(x, 1.7f, z, 0.18f, 2.8f, 0.18f, 0.18f, 0.20f, 0.22f);
        cajaGirada(x, 3.1f, z, 0.55f, 1.2f, 0.45f, 0.08f, 0.10f, 0.12f, giro);
        int activa = cruce.color(horizontal, tiempoUrbano);
        for (int indice = 0; indice < 3; indice++) {
            float brillo = indice == activa ? 1 : 0.12f;
            entero("uEmision", indice == activa ? 1 : 0);
            float rojo = indice < 2 ? brillo : 0;
            float verde = indice > 0 ? brillo : 0;
            float altura = 3.45f - indice * 0.35f;
            float focoX = x - (float) Math.sin(giro) * 0.24f;
            float focoZ = z - (float) Math.cos(giro) * 0.24f;
            cajaGirada(focoX, altura, focoZ, 0.28f, 0.25f, 0.06f, rojo, verde, 0.02f, giro);
        }
        entero("uEmision", 0);
    }

    private void dibujarSenales() {
        for (float x : new float[] {-44, 44}) {
            for (float z : new float[] {-44, 44}) {
                caja(x, 1.6f, z, 0.12f, 2.6f, 0.12f, 0.5f, 0.55f, 0.6f);
                entero("uEmision", 1);
                caja(x, 3, z, 1.2f, 1.3f, 0.12f, 0.05f, 0.25f, 0.85f);
                caja(x - 0.2f, 3, z - 0.08f, 0.12f, 0.85f, 0.03f, 1, 1, 1);
                caja(x + 0.05f, 3.35f, z - 0.08f, 0.5f, 0.12f, 0.03f, 1, 1, 1);
                caja(x + 0.05f, 3, z - 0.08f, 0.5f, 0.12f, 0.03f, 1, 1, 1);
                caja(x + 0.25f, 3.17f, z - 0.08f, 0.12f, 0.35f, 0.03f, 1, 1, 1);
                entero("uEmision", 0);
            }
        }
    }

    // ==================== 6. MINIMAPA: SEGUNDO PASE DE DIBUJO ====================

    /** Dibuja la escena principal y después la misma ciudad desde arriba, en un recuadro. */
    @Override // Amplía el cuadro completo definido en clase1.
    protected void dibujarFrame() {
        super.dibujarFrame(); // Dibuja primero la vista normal de la ciudad.
        if (enInicio || pausa) {
            dibujarMenu();
            if (glGetError() != GL_NO_ERROR) throw new IllegalStateException("Error OpenGL en menu");
            return;
        }
        if (mostrarMapa && Math.min(ancho, alto) >= 30) {
            dibujarMinimapa();
        }
        int[] r = PanelInfo.rectFreno(ancho, alto);
        panel.dibujarConMargen(ancho, alto, textoPanel(), programa, vao, r[2] + 32);
        botonFreno.dibujarBoton(ancho, alto,
            (frenoSolicitado() ? "FRENANDO" : "FRENO") + "\nMantener clic\nO tecla ESPACIO", programa, vao);
        if (tieneVictoria()) {
            cartelVictoria.dibujarCentrado(ancho, alto,
                resumenFinal(), programa, vao);
        }
        if (!tieneVictoria()) {
            navegador.dibujarRumbo(ancho, alto, rumboDestino(), textoRumbo(), programa, vao);
        }
        if ((avisoEntrega > 0 && !tieneVictoria()) || avisoChoque > 0) {
            int[] posicion = {ancho / 2 - Math.min(180, ancho / 3), alto - 88,
                              Math.min(360, ancho * 2 / 3), 65};
            aviso.dibujarEn(ancho, alto, posicion,
                avisoChoque > 0 ? "CONTACTO\nCambia de direccion" : "ENTREGA REALIZADA\nSigue al siguiente destino", programa, vao);
        }
        if (glGetError() != GL_NO_ERROR) {
            throw new IllegalStateException("Error OpenGL en interfaz");
        }
    }

    private void dibujarMenu() {
        float escala = Math.max(0.1f, Math.min(1, Math.min(ancho / 700f, alto / 600f)));
        int w = Math.round(600 * escala), h = Math.round(125 * escala);
        menuTitulo.dibujarEn(ancho, alto, new int[] {(ancho - w) / 2, alto / 2 + Math.round(50 * escala), w, h},
            enInicio ? "CIUDAD INTERACTIVA\nCompleta tres entregas en la ciudad\nConduce hasta el aro dorado y detente"
                     : "PAUSA\nTu recorrido esta guardado\nEnter o P para continuar", programa, vao);
        String[] textos = {enInicio ? "JUGAR\nEnter para comenzar" : "CONTINUAR\nVolver al recorrido",
                           "REINICIAR\nComenzar de nuevo", "SONIDO " + (silenciado ? "OFF" : "ON") + "\nV para alternar"};
        for (int i = 0; i < botonesMenu.length; i++) {
            botonesMenu[i].dibujarEn(ancho, alto, PanelInfo.rectMenu(ancho, alto, i), textos[i], programa, vao);
        }
    }

    private void dibujarMinimapa() {
        int dimensionMenor = Math.min(ancho, alto); // Busca la dimensión que limita el espacio disponible.
        int lado = Math.min(260, dimensionMenor / 3); // Limita el mapa a 260 píxeles y a un tercio de la ventana.
        int margen = Math.min(18, dimensionMenor / 20); // Calcula una separación adaptable respecto a los bordes.
        int x = ancho - lado - margen; // Ubica el recuadro cerca del borde derecho.
        int y = alto - lado - margen; // Ubica el recuadro arriba; OpenGL mide Y desde abajo.
        glEnable(GL_SCISSOR_TEST); // Activa el recorte para no borrar el resto de la escena.
        glScissor(x - 3, y - 3, lado + 6, lado + 6); // Selecciona el mapa más un borde de tres píxeles.
        glClearColor(0.8f, 0.87f, 0.94f, 1); // Define un color claro para el marco.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Borra solo el recuadro exterior gracias al scissor.
        glScissor(x, y, lado, lado); // Reduce el recorte al interior del minimapa.
        glClearColor(0.06f, 0.10f, 0.15f, 1); // Define el fondo oscuro del mapa.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Limpia color y profundidad dentro del mapa.
        glViewport(x, y, lado, lado); // Redirige la proyección al recuadro cuadrado.
        entero("uMapa", 1); // Selecciona la proyección ortográfica del shader de clase1.
        vistaMapa = true; // Indica a escena() que omita decoración pequeña y baliza flotante.
        try { // Asegura que el estado de dibujo se restaure incluso si el segundo pase falla.
            escena(); // Dibuja otra vez la misma ciudad, ahora vista desde arriba.
            dibujarIndicadorAuto(); // Resalta la posición y el frente del jugador en el mapa.
        } finally { // El siguiente cuadro debe volver a la configuración de pantalla completa.
            vistaMapa = false; // Reactiva los detalles de la escena principal.
            entero("uMapa", 0); // Recupera la proyección en perspectiva.
            glDisable(GL_SCISSOR_TEST); // Permite que la próxima limpieza abarque toda la pantalla.
            glViewport(0, 0, ancho, alto); // Recupera el área de dibujo de la ventana completa.
        }
        if (glGetError() != GL_NO_ERROR) { // Comprueba que el pase del mapa no haya generado errores OpenGL.
            throw new IllegalStateException("Error OpenGL en minimapa"); // Expone el error en la consola.
        }
    }

    protected String instruccionDestino() {
        if (tieneVictoria()) return "Misiones completadas";
        float distancia = VehiculoTrafico.distancia(autoX, autoZ, DESTINOS[entregas][0], DESTINOS[entregas][1]);
        return distancia < 7 ? "Detente para entregar" : "Destino a " + Math.round(distancia) + " m";
    }

    private String textoPanel() {
        String texto = "CIUDAD INTERACTIVA  |  " + (pausa ? "PAUSA" : "EN MARCHA");
        texto += "\nVELOCIDAD " + Math.round(Math.abs(velocidad) * 3.6f) + " km/h  |  " + (noche ? "NOCHE" : "DIA");
        texto += "\nFAROS " + (faros ? "ON" : "OFF") + "  |  ENTREGAS " + entregas + "/" + DESTINOS.length;
        texto += "\nCHOQUES " + choques;
        texto += "\n" + (tieneVictoria() ? "YOU WIN ! - R para jugar otra vez" : instruccionDestino()) + "  |  " + (int) tiempo + " s";
        texto += "\nH: ayuda  P: pausa  V: sonido " + (silenciado ? "OFF" : "ON");
        if (mostrarAyuda) {
            texto += "\nW/S o flechas: avanzar / retroceder";
            texto += "\nA/D: girar  ESPACIO: freno  C: camara";
            texto += "\nN: dia/noche  F: faros  M: minimapa";
            texto += "\nRaton derecho: girar / altura de vista";
            texto += "\nRe/Av Pag: altura  Inicio: centrar vista";
            texto += "\nR: reiniciar  ESC: salir  |  Norte arriba";
        }
        return texto;
    }

    /** Dibuja una marca cian y una punta blanca por encima de los edificios del minimapa. */
    private void dibujarIndicadorAuto() {
        cajaGirada(autoX, 25, autoZ, 2.2f, 0.1f, 3.2f, 0.1f, 1, 1, angulo); // Marca la posición con un rectángulo cian orientado.
        float frenteX = -(float) Math.sin(angulo); // Calcula la dirección frontal en el eje X.
        float frenteZ = -(float) Math.cos(angulo); // Calcula la dirección frontal en el eje Z.
        float puntaX = autoX + frenteX * 2; // Desplaza la punta dos unidades hacia delante en X.
        float puntaZ = autoZ + frenteZ * 2; // Desplaza la punta dos unidades hacia delante en Z.
        caja(puntaX, 26, puntaZ, 0.9f, 0.1f, 0.9f, 1, 1, 1); // Dibuja la punta blanca encima del indicador cian.
    }

    /** Punto de entrada del proyecto final. */
    public static void main(String[] args) {
        clase4 aplicacion = new clase4(); // Crea la versión con todas las etapas acumuladas.
        aplicacion.run(); // Inicia el ciclo de vida completo de la aplicación.
    }
}
