## Ejecutar

La pantalla inicial permite jugar con Enter. P abre la pausa con botones para continuar y reiniciar; V activa o silencia el sonido. Los destinos tienen un aro animado, flecha y avisos de entrega. Motor, frenado, entregas y choques usan audio generado por Java. 

Las aceras se pueden pisar; edificios y zonas verdes de los parques continúan siendo obstáculos.

Necesitas un JDK 17 o superior, Maven y una pantalla con soporte OpenGL 3.3. Abre una terminal en la carpeta que contiene `pom.xml`:

```powershell
mvn compile exec:exec -Pclase4
```

La primera compilación requiere descargar las dependencias Maven. Después puedes utilizar `mvn -o compile exec:exec -Pclase4` si ya están en la caché local. `exec:exec` solo no compila los cambios: utiliza el comando completo para no ejecutar clases antiguas.

En Windows también puedes ejecutar `run.bat 4`, `ejecutar.bat` o `./run.ps1 4`. El menú permite elegir cualquiera de las cuatro etapas. En VS Code, las tareas compilan antes de ejecutar; las configuraciones de depuración permiten iniciar cada clase. Esc cierra la ventana.

```powershell
mvn compile exec:exec -Pclase1
mvn compile exec:exec -Pclase2
mvn compile exec:exec -Pclase3
mvn compile exec:exec -Pclase4
```

Se mantienen los nombres originales, `extends`, llamadas a `super`, métodos cortos y comentarios en español. `caja()` y `cajaGirada()` siguen recibiendo posición XYZ, tamaño XYZ y color RGB; la segunda añade el giro. `pieza()` conserva su interfaz original.

Las clases auxiliares separan responsabilidades nuevas:

- `VehiculoTrafico`: estado, ruta, orientación, velocidad, frenado y comprobación de espacio de cada auto autónomo. No llama a OpenGL.
- `Cruce`: coordenadas, fase de semáforos y reserva de paso. La iluminación visible y la decisión del tráfico consultan el mismo estado.
- `PanelInfo`: textura de texto, rectángulo y recursos gráficos de la interfaz. No decide reglas del juego.
- `ModeloAuto`: construcción del deportivo principal, ruedas circulares y luces de freno/reversa.
- `ModelosEdificios`: casas, departamentos, torres y comercios por sector, sin letreros comerciales ni vitrinas.
- `Malla`: datos procedurales de prismas y cilindros, carga y liberación de VAO/VBO; comparte los shaders de la ciudad.

`dibujarVehiculo()` conserva el modelo compacto del tráfico y admite distintas posiciones y colores. `dibujarAuto()` delega en `ModeloAuto` para dibujar el deportivo principal. Las transformaciones no modifican temporalmente las variables del jugador.

## Diseño de la ciudad

La matriz tiene **11 × 11 celdas de 10 unidades**, con filas y columnas alternadas de calles conectadas. Hay **21 manzanas con edificios y 4 parques**. La zona original queda centrada y los sectores nuevos rodean esa base. El norte tiene casas con techos a dos aguas; el sur, comercios bajos con puertas opacas; los sectores intermedios, departamentos con balcones; y el centro, torres escalonadas. Hay variaciones deterministas de altura y color, patios, entradas y depósitos de azotea, sin anuncios comerciales ni vitrinas. Cada parque tiene cuatro árboles y un banco.

El suelo mide 110 × 110 unidades y los límites están a 55 unidades del origen. El centro de una celda se calcula con `centro(indice)`. La cámara general, el alcance de perspectiva y el minimapa reciben medidas relacionadas con el tamaño del mapa. La vista superior conserva el norte (-Z) arriba.

Hay 25 farolas, una junto a cada manzana. El shader usa normales transformadas, luz ambiental y direccional, luz difusa y atenuación por distancia. Las bombillas y las ventanas nocturnas tienen emisión visual; las farolas iluminan las superficies cercanas. Los dos faros acompañan al auto y su luz se desvanece entre 14 y 22 unidades. La cantidad de luces del shader se obtiene del arreglo Java. Las coordenadas de los faros se comparten con `ModeloAuto`, para que el haz coincida con las bombillas del deportivo.

## Controles

| Tecla | Acción | Desde |
|---|---|---|
| Izquierda / derecha | Orbitar la ciudad | Clase 1 |
| W / S o arriba / abajo | Avanzar / frenar y retroceder | Clase 2 |
| A / D o izquierda / derecha | Girar mientras el auto se mueve | Clase 2 |
| Espacio | Freno con parada completa | Clase 2 |
| Mantener clic en FRENO | Freno en pantalla; soltar libera el pedal | Clase 4 |
| C | Seguimiento / cámara general | Clase 2 |
| Botón derecho + arrastrar | Orbitar la vista cercana y subir/bajar su altura | Clase 2 |
| Re Pág / Av Pág | Subir / bajar la cámara cercana | Clase 2 |
| Inicio (Home) | Recuperar la vista cercana predeterminada | Clase 2 |
| R | Reiniciar auto; en clase4, partida, tráfico y semáforos | Clase 2 |
| N | Alternar día / noche | Clase 3 |
| F | Alternar faros | Clase 3 |
| M | Mostrar / ocultar minimapa | Clase 4 |
| H | Mostrar / ocultar ayuda dentro de la ventana | Clase 4 |
| P | Pausar / continuar la simulación completa | Clase 4 |
| Esc | Salir | Todas |

La pausa congela conducción, tráfico y ambos relojes. N, F, C, M y H pueden cambiar la presentación mientras está pausado. R sale de la pausa y comienza otra partida. Las preferencias de cámara, mapa y luces se conservan.

## Automovil del jugador

Carrocería roja baja con capó y luneta diferenciados, cabina inclinada, alerón, parrilla, espejos, manijas y placa posterior sin texto. Las ruedas son cilindros de 24 segmentos, con tapas metálicas y radios; giran según la distancia aceptada y las delanteras muestran el giro del volante. La conducción conserva las fórmulas originales. Las luces rojas aumentan al frenar con Espacio o al aplicar el pedal opuesto al movimiento; la reversa enciende luces blancas posteriores. F controla tanto el aspecto de las bombillas delanteras como el haz calculado.

La cámara cercana comienza a 8.5 unidades detrás y 2.8 de altura, mirando a 1.1 de altura. Se ajusta entre 1.6 y 8 con Re Pág/Av Pág o arrastrando verticalmente con el botón derecho; el arrastre horizontal orbita alrededor del auto. Inicio centra y restablece la vista sin reiniciar la partida. El brazo se acorta al encontrar una manzana edificada. Estos ajustes funcionan también durante la pausa; C sigue alternando la vista general. El radio de colisión permanece en 1.65: se verificaron las piezas y ruedas giradas para que el deportivo completo quepa dentro. El reinicio también restablece las animaciones. El tráfico autónomo mantiene su modelo compacto y sus reglas anteriores.

## Entregas e interfaz

El auto comienza en (-30, 30). Las tres entregas están en (50, -50), (-50, -50) y (-50, 50), dentro de los sectores ampliados. Para completar cada una, hay que acercarse a menos de 3 unidades y reducir la velocidad a menos de 1 unidad por segundo. La marca dorada aparece en la ciudad y en el minimapa; un rectángulo cian y una punta blanca indican al jugador y su orientación.

El botón FRENO está en la esquina inferior derecha. Mantener el clic izquierdo o Espacio reduce la velocidad hasta cero, tanto en avance como en reversa, y tiene prioridad sobre W/S. Al detenerse, soltar el freno no causa deriva: el auto permanece quieto hasta volver a acelerar. El frenado depende de deltaTime y permite reducir velocidad con pulsaciones breves.

El panel inferior muestra velocidad, ambiente, faros, entregas, tiempo, ayuda y estado de pausa. Al completar las tres misiones aparece un cartel grande y centrado con YOU WIN !, el tiempo final y R para volver a jugar. El cronómetro de la partida se detiene al ganar, pero los semáforos y el tráfico continúan con un reloj urbano independiente. R permite repetir las tres entregas.

El panel se adapta al espacio disponible, y el minimapa usa un viewport cuadrado y `glScissor` para proteger la vista principal. Para leer cómodamente la ayuda se recomienda una ventana de al menos 600 × 400; una ventana de pocos píxeles no permite texto legible.

## Tráfico y semáforos

Tres autos de distintos colores recorren rutas cerradas por avenidas exteriores, intermedias y centrales. Circulan por el carril derecho y toman curvas muestreadas de radio 2 en las esquinas. Cada uno conserva su posición, orientación, velocidad y avance en la ruta.

Las 36 intersecciones comparten un ciclo coordinado de 20 segundos: 7 segundos de verde, 2 de amarillo y 1 de despeje, primero para un eje y luego para el perpendicular. Los dos sentidos de un mismo eje tienen la misma fase. Las señales de cada acceso miran hacia los autos que se aproximan; se omiten postes fuera del mapa.

Los autónomos frenan antes del cruce en rojo. En amarillo mantienen la decisión de detenerse cuando hay distancia; si ya no pueden frenar con seguridad, completan el paso. Solo entran con permiso si la intersección y la salida están disponibles. Una reserva conservadora admite un vehículo a la vez; el auto ya admitido termina de despejar aunque la señal cambie. Mantienen separación respecto a otros autos y al jugador. La conducción usa deltaTime, aceleración y desaceleración; una comprobación final evita movimientos inválidos.

El jugador conserva conducción manual y puede ignorar un semáforo, pero las colisiones impiden atravesar un auto autónomo. Las señales P geométricas complementan la presentación urbana. Se retiraron las marcas cortas de parada pintadas frente a los cruces; las zonas de frenado del tráfico permanecen en su lógica. Los pasos peatonales se orientan a lo ancho de la calle junto a las esquinas, en lugar de ocupar solo una parte del carril.

## Verificación

```powershell
mvn test
```

Las pruebas no abren ventanas. Verifican cantidades y conectividad, obstáculos y bordes, entregas y reinicio, fases de semáforos, parada en rojo y amarillo, salida bloqueada, separación, continuidad en curvas y varias vueltas completas de tráfico. También comparan su movimiento con diferentes intervalos de actualización. `ModelosTest` comprueba normales de mallas, cuatro tipos de edificio, ocupación dentro de las manzanas, dimensiones del deportivo y respuesta de ruedas/freno/reinicio.

La verificación gráfica opcional abre una ventana, alterna controles y tamaños, comprueba la llegada y el reinicio y guarda capturas en `target/verificacion`:

```powershell
mvn test-compile exec:exec "-DmainClass=com.graphics.VerificacionGrafica" "-Dexec.classpathScope=test"
```

Ese programa coloca el auto en las entregas para verificar sus reglas; no sustituye conducir manualmente todo el recorrido. Para probar el arranque de una etapa y cerrar tras seis cuadros:

```powershell
mvn compile exec:exec -Pclase4 "-Ddemo.frames=6"
```

Guion manual: conducir por sectores nuevos; probar choques, reversa y C; comparar N/F; observar tráfico en rojo, verde y amarillo; bloquear una salida con el jugador y después retirarlo; alternar M/H/P; redimensionar; completar entregas y repetir tras R.

Para revisar el nuevo estilo con vistas de cerca del deportivo, los barrios y la ciudad completa:

```powershell
mvn test-compile exec:exec "-DmainClass=com.graphics.VerificacionEstilo" "-Dexec.classpathScope=test"
```

Las capturas se guardan en `target/estilo`. El dibujo sigue generándose con código; no se utiliza ImageGen ni modelos externos. La explicación de esta ampliación está en `docs/ESTILO_URBANO.md`.

## Limitaciones y procedencia

Se conserva iluminación local sin sombras ni oclusión: una luz puede atravesar edificios. No hay peatones, audio, daños ni física de choques; una colisión detiene al auto. Los autónomos tienen rutas predefinidas, no un planificador que busque desvíos. Si el jugador bloquea indefinidamente una calle o una salida, esperan hasta que la despeje. La reserva de un auto por cruce prioriza claridad y seguridad sobre capacidad de tráfico real. La actualización original limita pasos a 50 ms para evitar saltos; bajo frecuencias inferiores a 20 cuadros/s la simulación puede ralentizarse. El cielo y la iluminación cambian entre día y noche. Las ventanas son superficies opacas estilizadas: no hay interiores ni transparencia física.

## Entrega

Incluye `src`, `pom.xml`, este README, `docs`, los scripts y opcionalmente `.vscode`. Excluye `target`, `tmp`, dependencias descargadas y carpetas internas de herramientas. El código fuente es la entrega principal; `mvn package` genera el JAR y las bibliotecas en `target/lib` para uso local.
