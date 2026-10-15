# Pantalla: Agregar / Editar animal

Rutas: `animales/nuevo` (alta) y `animales/{codigo}/editar` (edición, paso 12). Reemplaza `AgregarAnimalActivity` + `activity_agregar_animal.xml`. Paquete `ui/feature/animales/formulario`. Sin barra inferior.

## Propósito
Registrar un animal en menos de un minuto, en el corral, con una mano y sin internet.

## Wireframe
```text
┌────────────────────────────────────┐
│ (X Cerrar)  Nuevo animal           │
├────────────────────────────────────┤
│ Identificación                     │  FormSection
│ Código o arete *                   │
│ [ (Codigo) 001                  ]  │  KeyboardType.Text, mayúsculas
│ Nombre (opcional)                  │
│ [ (Bovino) Lucero               ]  │
│                                    │
│ Tipo *                             │
│ [Vaca][Toro][Ternero][Ternera]     │  ChoiceChipGroup 48dp (2x2 si no cabe)
│ Sexo *                             │
│ [Hembra]  [Macho]                  │  ChoiceChipGroup, auto según tipo
│ Raza *                             │
│ [ (Raza) Holstein            v  ]  │  AgroDropdownField
│                                    │
│ Datos físicos                      │
│ Peso *                             │
│ [ (Peso) 480               kg   ]  │  NumberField Decimal
│ Fecha de nacimiento *              │
│ [ (Fecha) 15/03/2020            ]  │  DateField -> DatePickerDialog
│                                    │
│ Ubicación                          │
│ Potrero *                          │
│ [ (Potrero) Potrero 1        v  ]  │  AgroDropdownField
│ Observaciones                      │
│ [                               ]  │  multilinea 3-5 líneas
│                                    │
├────────────────────────────────────┤
│ [        Guardar animal        ]   │  PrimaryButton fijo abajo (bottomBar)
└────────────────────────────────────┘
```
El botón Guardar va en un `bottomBar` del Scaffold (siempre visible sobre el teclado con `imePadding`).

## Campos (binding a `Animal`)
| Campo UI | Propiedad | Opciones / regla (idénticas al código actual) |
|---|---|---|
| Código o arete | `codigo` | Obligatorio, `trim()`. Único (índice unique). |
| Nombre | `nombre` | Opcional, `trim()` |
| Tipo | `tipo` | Vaca, Toro, Ternero, Ternera. Obligatorio |
| Sexo | `sexo` | Hembra, Macho. Obligatorio. Sugerencia automática: Vaca/Ternera -> Hembra, Toro/Ternero -> Macho (editable) |
| Raza | `raza` | Holstein, Jersey, Brahman, Normando, Pardo Suizo, Criollo, Otra. Obligatorio |
| Peso | `peso` | `toDoubleOrNull()` (aceptar coma), > 0. Obligatorio |
| Fecha de nacimiento | `fechaNacimiento` | Obligatorio, guardar `yyyy-MM-dd`, no futura |
| Potrero | `potrero` | Opciones = `LocalCacheDao.allPotreros().map { it.nombre }`. Si la caché está vacía: lista de respaldo actual (Potrero 1, 2, 3) y nota "Sincroniza para ver tus potreros". Obligatorio |
| Observaciones | `observacion` | Opcional |

`syncState = PENDING` lo asigna `AnimalRepository.insert` (no la UI).

```kotlin
data class AnimalFormUiState(
    val modo: Modo = Modo.NUEVO,           // NUEVO | EDITAR
    val codigo: String = "", val nombre: String = "", val tipo: String? = null, val sexo: String? = null,
    val raza: String? = null, val pesoTexto: String = "", val fechaNacimiento: LocalDate? = null,
    val potrero: String? = null, val observacion: String = "",
    val potrerosDisponibles: List<String> = emptyList(), val potrerosDesdeCache: Boolean = false,
    val errores: Map<Campo, UiText> = emptyMap(), val isSaving: Boolean = false, val hayCambios: Boolean = false,
)
```

## Validación
- Al tocar Guardar validar **todo** a la vez (no uno por uno con Toast como hoy); mostrar error bajo cada campo, hacer scroll y foco al primero con error, y anunciar "Revisa N campos".
- Mensajes: "Escribe el código", "Elige el tipo", "Elige el sexo", "Elige la raza", "Escribe un peso mayor que cero", "Elige la fecha", "Elige el potrero".
- Código duplicado: `repository.insert` lanza excepción (ABORT por índice unique): mostrar en el campo Código "Ya existe un animal con este código".

## Interacciones
- Guardar OK -> `popBackStack()` y Snackbar en la lista: "Animal guardado en el teléfono. Se enviará cuando haya internet." (si hay red y la sync termina: "Animal guardado y enviado").
- Cerrar/Atrás con `hayCambios` -> `ConfirmDialog("¿Descartar cambios?", destructive = true)`.
- Estado preservado con `SavedStateHandle` al rotar o ir a segundo plano.

## Edición (paso 12)
- Precarga por `findByCodigo`. Título "Editar animal". El campo Código queda **solo lectura** (es la clave de búsqueda y de la ruta).
- Guarda con `AnimalRepository.update` (adición permitida, ver 02 §8). Si el backend no soporta UPDATE, la ruta no se expone.

## Criterios de aceptación
- [ ] Mismas reglas de validación que `guardarAnimal()` original, mostradas en línea.
- [ ] Guardar en modo avión funciona y el animal aparece en la lista con "Por enviar".
- [ ] Potreros reales desde caché cuando existen.
- [ ] Campo peso abre teclado numérico; fecha abre selector.
- [ ] Todos los controles >= 48 dp; botón Guardar 56 dp siempre visible.

## Actualización Figma (prevalece donde difiera)

Leyenda: [Existe] modelo/API actual · [UI] solo interfaz o preferencia local · [Backend] requiere campo/endpoint/outbox nuevo (ver 00-README §6.3).

Frame 9 "Agregar animal". Orden de campos del Figma adoptado: foto, código, tipo, raza, peso, sexo, fecha de nacimiento, potrero, (observación, propia), Guardar.

| Campo Figma | Decisión | Datos |
|---|---|---|
| Agregar foto | `PhotoPickerField` (03 §11) arriba del formulario: "Agregar foto" con opciones Cámara / Galería (`PickVisualMedia`, `TakePicture`). Comprimir a 1280 px JPEG 80%, guardar en `filesDir/fotos/{codigo}.jpg` | [Backend] campo `fotoUri` local + subida; oculto con flag `FOTOS_ANIMAL` |
| Código | Igual | [Existe] |
| Tipo | `ChoiceChipGroup` | [Existe] |
| Raza | `AgroDropdownField` | [Existe] |
| Peso | `NumberField` kg | [Existe] |
| Sexo | `ChoiceChipGroup` (después de peso como en Figma; autocompletado por tipo) | [Existe] |
| Fecha de nacimiento | `DateField` | [Existe] |
| Potrero (select) | `AgroDropdownField` desde caché | [Existe] |
| Guardar | `PrimaryButton` fijo "Guardar animal" | [Existe] |

Mantener "Nombre (opcional)" y "Observaciones" aunque el Figma no los muestre: existen en `Animal` y en el payload de sync.
