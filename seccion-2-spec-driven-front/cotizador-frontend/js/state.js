/**
 * state.js
 *
 * Estado privado de la aplicación (Module pattern con ES Modules).
 * Este módulo NO importa nada, NO toca el DOM, NO hace fetch y NO
 * contiene ninguna regla de cálculo de negocio: todos los valores
 * numéricos (totales, subtotales, recargos, tiempos estimados)
 * llegan ya calculados desde el backend y acá solo se guardan tal cual.
 */

const state = {
  // Catálogos cargados desde la API
  tiposCalzado: [], // Array<{ id: string, nombre: string, factorComplejidad: string }>
  tiposReparacion: [], // Array<{ id: string, nombre: string, precioBase: string, tiempoEstimadoDias: number }>
  catalogosCargados: false,

  // Selección actual del usuario en el formulario
  seleccion: {
    tipoCalzadoId: '', // string — id seleccionado en el <select>
    tipoReparacionIds: [], // string[] — ids de checkboxes marcados
    urgente: false, // boolean
  },

  // Última respuesta recibida del POST /api/cotizaciones
  ultimaCotizacion: null, // CotizacionResponse | null

  // Estado de la petición en curso
  cargando: false,
};

// Campos válidos para setSeleccion
const CAMPOS_SELECCION_VALIDOS = ['tipoCalzadoId', 'tipoReparacionIds', 'urgente'];

/**
 * Guarda los catálogos obtenidos desde la API y marca que ya fueron cargados.
 * Los arrays se copian (spread) para no conservar la referencia externa.
 * @param {Array<Object>} tiposCalzado - Catálogo de tipos de calzado.
 * @param {Array<Object>} tiposReparacion - Catálogo de tipos de reparación.
 * @returns {void}
 */
export function setCatalogos(tiposCalzado, tiposReparacion) {
  state.tiposCalzado = [...tiposCalzado];
  state.tiposReparacion = [...tiposReparacion];
  state.catalogosCargados = true;
}

/**
 * Devuelve los catálogos actuales, copiados para evitar mutaciones externas
 * del estado interno.
 * @returns {{ tiposCalzado: Array<Object>, tiposReparacion: Array<Object> }}
 */
export function getCatalogos() {
  return {
    tiposCalzado: [...state.tiposCalzado],
    tiposReparacion: [...state.tiposReparacion],
  };
}

/**
 * Indica si los catálogos ya fueron cargados desde la API.
 * @returns {boolean}
 */
export function isCatalogoCargado() {
  return state.catalogosCargados;
}

/**
 * Actualiza un campo de la selección actual del formulario.
 * Solo acepta 'tipoCalzadoId', 'tipoReparacionIds' y 'urgente'; cualquier
 * otro campo se ignora silenciosamente.
 * INVARIANTE: tipoReparacionIds siempre queda como array (nunca null/undefined).
 * @param {'tipoCalzadoId'|'tipoReparacionIds'|'urgente'} campo - Campo a actualizar.
 * @param {*} valor - Nuevo valor para el campo.
 * @returns {void}
 */
export function setSeleccion(campo, valor) {
  if (!CAMPOS_SELECCION_VALIDOS.includes(campo)) {
    return;
  }

  if (campo === 'tipoReparacionIds') {
    state.seleccion.tipoReparacionIds = Array.isArray(valor) ? [...valor] : [];
    return;
  }

  if (campo === 'urgente') {
    state.seleccion.urgente = Boolean(valor);
    return;
  }

  if (campo === 'tipoCalzadoId') {
    state.seleccion.tipoCalzadoId = String(valor);
  }
}

/**
 * Devuelve una copia de la selección actual del formulario.
 * @returns {{ tipoCalzadoId: string, tipoReparacionIds: string[], urgente: boolean }}
 */
export function getSeleccion() {
  return {
    tipoCalzadoId: state.seleccion.tipoCalzadoId,
    tipoReparacionIds: [...state.seleccion.tipoReparacionIds],
    urgente: state.seleccion.urgente,
  };
}

/**
 * Restablece la selección del formulario a sus valores iniciales.
 * @returns {void}
 */
export function resetSeleccion() {
  state.seleccion = {
    tipoCalzadoId: '',
    tipoReparacionIds: [],
    urgente: false,
  };
}

/**
 * Guarda la última cotización recibida del backend. Acepta null explícito
 * para limpiar el resultado previo (REQ-026).
 * @param {Object|null} cotizacion - Respuesta de POST /api/cotizaciones, o null.
 * @returns {void}
 */
export function setUltimaCotizacion(cotizacion) {
  state.ultimaCotizacion = cotizacion;
}

/**
 * Devuelve la última cotización guardada, o null si no hay ninguna.
 * @returns {Object|null}
 */
export function getUltimaCotizacion() {
  return state.ultimaCotizacion;
}

/**
 * Actualiza el estado de carga de la petición en curso.
 * INVARIANTE: solo se puede poner en true si los catálogos ya fueron
 * cargados (catalogosCargados === true); poner en false siempre se aplica.
 * @param {boolean} valor - Nuevo valor de carga.
 * @returns {void}
 */
export function setCargando(valor) {
  if (valor === false) {
    state.cargando = false;
    return;
  }

  if (!state.catalogosCargados) {
    return;
  }

  state.cargando = true;
}

/**
 * Indica si hay una petición en curso.
 * @returns {boolean}
 */
export function isCargando() {
  return state.cargando;
}
