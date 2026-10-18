package com.graphics; // Agrupa las cuatro lecciones dentro del mismo paquete.

import static org.lwjgl.glfw.GLFW.*; // Permite consultar teclas y cambiar el título de la ventana.

/**
 * CLASE 2: CREACIÓN Y MOVIMIENTO DEL AUTO.
 * Hereda la ciudad de clase1 y agrega únicamente lo necesario para conducir.
 * Orden de lectura: variables, teclado, movimiento, colisiones, cámara y dibujo.
 */
public class clase2 extends clase1 {

    // ==================== 1. VARIABLES DEL AUTO ====================
    protected float autoX = -30; // Posición horizontal inicial: centro de una calle.
    protected float autoZ = 30; // Posición inicial sobre el eje que recorre el fondo de la ciudad.
    protected float angulo = 0; // Orientación en radianes; cero apunta hacia -Z.
    protected float velocidad = 0; // Unidades por segundo; un valor negativo significa reversa.
    protected boolean camaraAerea = false; // false: seguir el auto; true: observar toda la ciudad.
    protected static final float RADIO_AUTO = 1.62f; // Radio que contiene al vehículo para las colisiones.
    protected boolean frenando = false;
    protected float giroRuedas = 0;
    protected float rotacionRuedas = 0;
    protected float sacudidaCamara = 0;
    protected boolean contactoActual;
    private final ModeloAuto modeloAuto = new ModeloAuto(this);
    protected final Chata chata = new Chata(autoX, autoZ, angulo);
    protected float alturaCamara = 2.8f;
    protected float orbitaSeguimiento = 0;
    private boolean arrastrandoVista = false;
    private boolean primerMovimiento = true;
    private double ratonAnteriorX;
    private double ratonAnteriorY;

    // ==================== 2. TECLADO Y REINICIO ====================

    /** Atiende acciones que deben ocurrir una sola vez por pulsación. */
    @Override // Sustituye el método de clase1 y conserva sus acciones mediante super.
    protected void tecla(int key) {
        super.tecla(key); // Ejecuta el control ESC definido en clase1.

        if (key == GLFW_KEY_C) { // Comprueba si se presionó la tecla de cámara.
            camaraAerea = !camaraAerea; // Invierte el modo de cámara actual.
            primerMovimiento = true;
        }

        if (key == GLFW_KEY_HOME) {
            reiniciarVista();
        }

        if (key == GLFW_KEY_R) { // Comprueba si el usuario quiere comenzar de nuevo.
            reiniciar(); // Restaura las variables de conducción.
        }
    }

    /** Coloca nuevamente el auto en su punto de partida. */
    protected void reiniciar() {
        autoX = -30; // Recupera la coordenada X de inicio.
        autoZ = 30; // Recupera la coordenada Z de inicio.
        angulo = 0; // Orienta el frente hacia -Z.
        velocidad = 0; // Detiene cualquier movimiento previo.
        frenando = false;
        giroRuedas = 0;
        rotacionRuedas = 0;
        chata.reiniciar(autoX, autoZ, angulo);
    }

    // ==================== 3. MOVIMIENTO POR CUADRO ====================

    /** Actualiza la conducción; deltaTime contiene los segundos transcurridos entre cuadros. */
    @Override // Cambia la cámara orbital de clase1 por el control del vehículo.
    protected void actualizar(float deltaTime) {
        contactoActual = false;
        actualizarVista(deltaTime);
        float acelerador = 0; // Sin teclas pulsadas no se aplica aceleración del motor.

        if (pulsada(GLFW_KEY_W) || pulsada(GLFW_KEY_UP)) { // Acepta W o flecha arriba para avanzar.
            acelerador += 1; // Solicita aceleración hacia delante.
        }

        if (pulsada(GLFW_KEY_S) || pulsada(GLFW_KEY_DOWN)) { // Acepta S o flecha abajo para retroceder.
            acelerador -= 1; // Primero reduce la velocidad positiva y después entra en reversa.
        }

        boolean freno = frenoSolicitado();
        frenando = freno || acelerador * velocidad < -0.2f;
        if (freno) {
            float restante = Math.max(0, Math.abs(velocidad) - 18 * deltaTime);
            velocidad = Math.copySign(restante, velocidad);
        } else {
            velocidad += acelerador * 9 * deltaTime;
            velocidad *= (float) Math.exp(-0.7f * deltaTime);
        }
        if ((freno || acelerador == 0) && Math.abs(velocidad) < 0.05f) {
            velocidad = 0;
        }
        velocidad = Math.max(-6, Math.min(16, velocidad)); // Limita la reversa a -6 y el avance a 16.
        float direccion = 0; // Sin dirección presionada el volante permanece recto.

        if (pulsada(GLFW_KEY_A) || pulsada(GLFW_KEY_LEFT)) { // Comprueba el giro hacia la izquierda.
            direccion += 1; // Selecciona el sentido positivo de rotación.
        }

        if (pulsada(GLFW_KEY_D) || pulsada(GLFW_KEY_RIGHT)) { // Comprueba el giro hacia la derecha.
            direccion -= 1; // Selecciona el sentido negativo de rotación.
        }

        float anguloAnterior = angulo;
        angulo += direccion * velocidad * 0.11f * deltaTime; // Gira según la velocidad; en reversa invierte el giro.
        giroRuedas += (direccion * 0.24f - giroRuedas) * (1 - (float) Math.exp(-10 * deltaTime));
        float frenteX = -(float) Math.sin(angulo); // Obtiene la componente X del frente del vehículo.
        float frenteZ = -(float) Math.cos(angulo); // Obtiene la componente Z; con ángulo cero vale -1.
        float siguienteX = autoX + frenteX * velocidad * deltaTime; // Propone la nueva posición X.
        float siguienteZ = autoZ + frenteZ * velocidad * deltaTime; // Propone la nueva posición Z.

        float[] propuestaChata = chata.proponer(siguienteX, siguienteZ, angulo);
        if (puedeCircular(siguienteX, siguienteZ) && chata.puedeMover(propuestaChata, angulo, this)) { // Comprueba la posición antes de mover el auto.
            autoX = siguienteX; // Acepta el desplazamiento horizontal.
            autoZ = siguienteZ;
            chata.aceptar(propuestaChata); // Acepta el desplazamiento en profundidad.
            rotacionRuedas = (rotacionRuedas + velocidad * deltaTime / ModeloAuto.RADIO_RUEDA)
                             % ((float) Math.PI * 2);
        } else { // La posición propuesta invadiría una manzana o saldría del mapa.
            angulo = anguloAnterior;
            contactoActual = true;
            alChocar(Math.abs(velocidad));
            velocidad = 0; // Detiene el auto conservando su última posición válida.
        }

        actualizarTitulo(); // Muestra los controles y la velocidad actual.
    }

    /** Compone el texto de la ventana sin mezclarlo con las fórmulas de conducción. */
    protected void actualizarTitulo() {
        int kilometrosPorHora = Math.round(Math.abs(velocidad) * 3.6f); // Convierte m/s a km/h y redondea.
        String titulo = getClass().getSimpleName(); // Obtiene el nombre de la etapa que se está ejecutando.
        titulo += " | C: camara | R: reiniciar | ";
        titulo += kilometrosPorHora + " km/h"; // Añade la magnitud de la velocidad.
        titulo += estadoExtra(); // Permite a clase3 y clase4 añadir luces y entregas.
        glfwSetWindowTitle(ventana, titulo); // Publica el texto en la barra superior de la ventana.
    }

    /** Deja un espacio para los indicadores de las próximas lecciones. */
    protected String estadoExtra() {
        return ""; // En clase2 todavía no hay información adicional.
    }

    // ==================== 4. COLISIONES CON LA CIUDAD ====================

    /** Comprueba si el círculo del auto cabe en una posición sin tocar manzanas ni bordes. */
    protected boolean puedeCircular(float x, float z) {
        return puedeCircularCiudad(x, z);
    }

    protected boolean puedeCircularCiudad(float x, float z) {
        return puedeCircularCiudad(x, z, RADIO_AUTO);
    }

    protected boolean puedeCircularChata(float x, float z, float radio) {
        return puedeCircularCiudad(x, z, radio);
    }

    protected boolean puedeCircularCiudad(float x, float z, float radio) {
        float limitePermitido = LIMITE - radio; // Reserva espacio para que el auto completo quede dentro.

        if (Math.abs(x) > limitePermitido || Math.abs(z) > limitePermitido) { // Detecta salida por cualquier borde.
            return false; // Rechaza la posición exterior.
        }

        for (int fila = 0; fila < MAPA.length; fila++) { // Recorre las filas del mapa.
            for (int columna = 0; columna < MAPA[fila].length; columna++) { // Recorre las celdas de cada fila.
                if (MAPA[fila][columna] == 0) { // Una celda con cero representa calle.
                    continue; // Omite la calle porque no es un obstáculo.
                }

                float[][] solidos = MAPA[fila][columna] == 2
                    ? new float[][] {{centro(columna), centro(fila), 9, 9}}
                    : ModelosEdificios.obstaculos(fila, columna);
                for (float[] solido : solidos) {
                    float cercaX = Math.max(solido[0] - solido[2] / 2, Math.min(x, solido[0] + solido[2] / 2));
                    float cercaZ = Math.max(solido[1] - solido[3] / 2, Math.min(z, solido[1] + solido[3] / 2));
                    float dx = x - cercaX, dz = z - cercaZ;
                    if (dx * dx + dz * dz < radio * radio) return false;
                }
            }
        }

        return true; // Acepta la posición porque no se encontró ningún obstáculo.
    }

    protected float alturaSuelo(float x, float z) {
        int columna = (int) Math.floor((x + LIMITE) / CELDA);
        int fila = (int) Math.floor((z + LIMITE) / CELDA);
        return fila >= 0 && fila < MAPA.length && columna >= 0 && columna < MAPA[fila].length
            && MAPA[fila][columna] != 0 ? 0.3f : 0;
    }

    // ==================== 5. CÁMARA ====================

    protected void reiniciarVista() {
        alturaCamara = 2.8f;
        orbitaSeguimiento = 0;
        primerMovimiento = true;
    }

    protected void ajustarVista(float giro, float altura) {
        orbitaSeguimiento += giro;
        orbitaSeguimiento = (float) Math.atan2(Math.sin(orbitaSeguimiento), Math.cos(orbitaSeguimiento));
        alturaCamara = Math.max(1.6f, Math.min(8, alturaCamara + altura));
    }

    protected void actualizarVista(float deltaTime) {
        if (camaraAerea) {
            return;
        }
        float cambio = 0;
        if (pulsada(GLFW_KEY_PAGE_UP)) {
            cambio += 3 * Math.max(0, deltaTime);
        }
        if (pulsada(GLFW_KEY_PAGE_DOWN)) {
            cambio -= 3 * Math.max(0, deltaTime);
        }
        ajustarVista(0, cambio);
    }

    @Override
    protected void botonRaton(int boton, int accion) {
        if (boton == GLFW_MOUSE_BUTTON_RIGHT) {
            arrastrandoVista = accion == GLFW_PRESS;
            primerMovimiento = true;
        }
    }

    @Override
    protected void moverRaton(double x, double y) {
        if (arrastrandoVista && !camaraAerea && !primerMovimiento) {
            ajustarVista((float) (x - ratonAnteriorX) * 0.006f,
                         (float) (ratonAnteriorY - y) * 0.015f);
        }
        ratonAnteriorX = x;
        ratonAnteriorY = y;
        primerMovimiento = false;
    }

    protected float distanciaVista(float orientacion) {
        for (float distancia = 0.5f; distancia <= 8.5f; distancia += 0.25f) {
            float x = autoX + (float) Math.sin(orientacion) * distancia;
            float z = autoZ + (float) Math.cos(orientacion) * distancia;
            for (int fila = 0; fila < MAPA.length; fila++) {
                for (int columna = 0; columna < MAPA[fila].length; columna++) {
                    if (MAPA[fila][columna] == 1 && Math.abs(x - centro(columna)) < 4.35f
                        && Math.abs(z - centro(fila)) < 4.35f) {
                        return Math.max(2, distancia - 0.5f);
                    }
                }
            }
        }
        return 8.5f;
    }

    /** Elige entre una vista general y una cámara situada detrás del auto. */
    @Override // Personaliza la cámara creada en clase1.
    protected void configurarCamara() {
        if (camaraAerea) { // Comprueba si está activa la vista general.
            super.configurarCamara(); // Reutiliza la cámara oblicua de la primera lección.
            return; // Evita reemplazarla con la cámara de seguimiento.
        }

        float orientacionCamara = angulo + orbitaSeguimiento;
        float distancia = distanciaVista(orientacionCamara);
        float camaraX = autoX + (float) Math.sin(orientacionCamara) * distancia;
        float camaraZ = autoZ + (float) Math.cos(orientacionCamara) * distancia;
        vector("uOjo", camaraX + sacudidaCamara, alturaCamara + alturaSuelo(autoX, autoZ), camaraZ);
        vector("uObjetivo", autoX, 1.1f + alturaSuelo(autoX, autoZ), autoZ);
        decimal("uAspecto", (float) ancho / alto); // Mantiene las proporciones al redimensionar la ventana.
    }

    // ==================== 6. DIBUJO DEL AUTO ====================

    /** Dibuja primero la ciudad existente y luego el vehículo. */
    @Override // Amplía el dibujo de clase1 sin copiar su código.
    protected void escena() {
        super.escena(); // Dibuja asfalto, calles, edificios y parques.
        dibujarAuto(); // Añade el modelo del vehículo sobre la ciudad.
    }

    /** Construye el auto con cajas; argumentos: posición XYZ, tamaño XYZ y color RGB. */
    protected void dibujarAuto() {
        chata.dibujar(this);
        modeloAuto.dibujar();
    }

    protected boolean farosEncendidos() {
        return true;
    }

    protected void alChocar(float intensidad) { }

    protected boolean frenoSolicitado() {
        return pulsada(GLFW_KEY_SPACE);
    }

    protected void dibujarVehiculo(float posX, float posZ, float giro, float rojo, float verde, float azul) {
        piezaVehiculo(posX, posZ, giro, 0, 0.65f, 0, 1.65f, 0.55f, 2.6f, rojo, verde, azul); // Dibuja la carrocería roja.
        piezaVehiculo(posX, posZ, giro, 0, 1.12f, 0.12f, 1.3f, 0.55f, 1.25f, 0.22f, 0.65f, 0.78f); // Dibuja la cabina azulada.
        float[] ladosRuedas = {-0.88f, 0.88f}; // Ubica ruedas a izquierda y derecha del auto.
        float[] ejesRuedas = {-0.82f, 0.82f}; // Ubica las ruedas delanteras y traseras.

        for (float x : ladosRuedas) { // Selecciona uno de los dos lados del vehículo.
            for (float z : ejesRuedas) { // Selecciona el eje delantero o trasero.
                piezaVehiculo(posX, posZ, giro, x, 0.38f, z, 0.24f, 0.58f, 0.6f, 0.055f, 0.065f, 0.08f); // Dibuja una rueda oscura.
            }
        }

        float[] ladosFaros = {-0.55f, 0.55f}; // Define la separación lateral de las luces.
        for (float x : ladosFaros) { // Repite el dibujo para ambos lados.
            piezaVehiculo(posX, posZ, giro, x, 0.68f, -1.32f, 0.38f, 0.2f, 0.07f, 1, 0.95f, 0.65f); // Dibuja un faro delantero claro.
            piezaVehiculo(posX, posZ, giro, x, 0.68f, 1.32f, 0.35f, 0.17f, 0.07f, 0.85f, 0.05f, 0.05f); // Dibuja una luz trasera roja.
        }
    }

    /** Transforma una pieza del espacio local del auto al espacio de la ciudad. */
    protected void pieza(float x, float y, float z, float sx, float sy, float sz, float r, float g, float b) {
        piezaVehiculo(autoX, autoZ, angulo, x, y, z, sx, sy, sz, r, g, b);
    }

    protected void piezaVehiculo(float posX, float posZ, float giro, float x, float y, float z,
                                  float sx, float sy, float sz, float r, float g, float b) {
        float coseno = (float) Math.cos(giro); // Calcula el coseno de la orientación del auto.
        float seno = (float) Math.sin(giro); // Calcula el seno de la misma orientación.
        float mundoX = posX + coseno * x + seno * z; // Gira la posición local y suma la posición X del auto.
        float mundoZ = posZ - seno * x + coseno * z; // Gira la posición local y suma la posición Z del auto.
        cajaGirada(mundoX, y + alturaSuelo(posX, posZ), mundoZ, sx, sy, sz, r, g, b, giro); // Dibuja la pieza con la orientación del vehículo.
    }

    /** Punto de entrada para ejecutar únicamente la segunda etapa. */
    public static void main(String[] args) {
        clase2 aplicacion = new clase2(); // Crea la aplicación con ciudad y vehículo.
        aplicacion.run(); // Inicia la ventana y el ciclo de dibujo heredados.
    }
}
