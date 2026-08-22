// URL base de la API. Se deja vacía a propósito: el frontend se sirve
// detrás de un proxy Nginx en el mismo origen que el backend, por lo que
// alcanza con rutas relativas (por ejemplo '/api/tipos-calzado').
const API_BASE_URL = '';

/**
 * Centraliza la interpretación de la respuesta HTTP del backend y la
 * normaliza a un objeto de error uniforme cuando corresponde.
 * No se exporta: es un detalle interno de este módulo.
 *
 * @param {Response} response
 * @returns {Promise<any>} el cuerpo parseado como JSON cuando la respuesta es exitosa
 */
async function _handleResponse(response) {
  if (response.ok) {
    return response.json();
  }

  let body = null;
  try {
    body = await response.json();
  } catch {
    body = null;
  }

  if (response.status === 400) {
    return Promise.reject({
      tipo: 'validacion',
      mensaje: (body && body.mensaje) || 'Los datos enviados no son válidos. Por favor, revíselos e intente nuevamente.',
      codigo: 400,
    });
  }

  if (response.status === 404) {
    return Promise.reject({
      tipo: 'no_encontrado',
      mensaje: (body && body.mensaje) || 'El recurso solicitado no fue encontrado.',
      codigo: 404,
    });
  }

  if (response.status === 500) {
    return Promise.reject({
      tipo: 'servidor',
      mensaje: 'Ocurrió un problema en el servidor. Por favor, intente nuevamente.',
      codigo: 500,
    });
  }

  return Promise.reject({
    tipo: 'servidor',
    mensaje: 'Ocurrió un error inesperado. Por favor, intente nuevamente.',
    codigo: response.status,
  });
}

/**
 * Obtiene la lista de tipos de calzado disponibles.
 * GET /api/tipos-calzado
 * @returns {Promise<Array<{id, nombre, factorComplejidad}>>}
 */
export async function getTiposCalzado() {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}/api/tipos-calzado`);
  } catch {
    return Promise.reject({
      tipo: 'red',
      mensaje: 'No fue posible conectar con el servidor. Verifique su conexión a internet.',
      codigo: null,
    });
  }
  return _handleResponse(response);
}

/**
 * Obtiene la lista de tipos de reparación disponibles.
 * GET /api/tipos-reparacion
 * @returns {Promise<Array<{id, nombre, precioBase, tiempoEstimadoDias}>>}
 */
export async function getTiposReparacion() {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}/api/tipos-reparacion`);
  } catch {
    return Promise.reject({
      tipo: 'red',
      mensaje: 'No fue posible conectar con el servidor. Verifique su conexión a internet.',
      codigo: null,
    });
  }
  return _handleResponse(response);
}

/**
 * Envía los datos de una cotización al backend para que sea calculada.
 * POST /api/cotizaciones
 * @param {{ tipoCalzadoId: string, tipoReparacionIds: string[], urgente: boolean }} payload
 * @returns {Promise<CotizacionResponse>}
 */
export async function postCotizacion(payload) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}/api/cotizaciones`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        tipoCalzadoId: payload.tipoCalzadoId,
        tipoReparacionIds: payload.tipoReparacionIds,
        urgente: payload.urgente,
      }),
    });
  } catch {
    return Promise.reject({
      tipo: 'red',
      mensaje: 'No fue posible conectar con el servidor. Verifique su conexión a internet.',
      codigo: null,
    });
  }
  return _handleResponse(response);
}
