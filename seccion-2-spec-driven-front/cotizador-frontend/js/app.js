// app.js — punto de entrada de la aplicación. Único módulo que toca el DOM.
// Orquesta la carga de catálogos, el formulario y el render de resultados/errores.
// No realiza cálculos de negocio: todos los valores numéricos llegan ya calculados
// y formateados desde el backend, y se insertan tal cual.

import {
  setCatalogos,
  getCatalogos,
  isCatalogoCargado,
  setSeleccion,
  getSeleccion,
  setUltimaCotizacion,
  getUltimaCotizacion,
  setCargando,
} from './state.js';

import { getTiposCalzado, getTiposReparacion, postCotizacion } from './api.js';

// Mensaje por defecto cuando falla la carga de catálogos (REQ-006).
const MENSAJE_ERROR_CATALOGO = 'No fue posible cargar los catálogos. Recargue la página.';
const MENSAJE_ERROR_GENERICO = 'Ocurrió un error inesperado. Intente nuevamente.';

// Referencias del DOM, cacheadas una sola vez en inicializar().
let catalogoError;
let formCotizacion;
let tipoCalzadoSelect;
let tipoCalzadoCampo;
let tipoCalzadoError;
let reparacionesFieldset;
let reparacionesLista;
let reparacionesError;
let urgenteCheckbox;
let btnCotizar;
let btnTexto;
let btnSpinner;
let resultado;

// REQ-003: al cargar el DOM, se dispara la inicialización de la app.
function inicializar() {
  catalogoError = document.getElementById('catalogo-error');
  formCotizacion = document.getElementById('form-cotizacion');
  tipoCalzadoSelect = document.getElementById('tipo-calzado');
  tipoCalzadoCampo = tipoCalzadoSelect.closest('.campo');
  tipoCalzadoError = document.getElementById('tipo-calzado-error');
  reparacionesFieldset = document.getElementById('reparaciones-fieldset');
  reparacionesLista = document.getElementById('reparaciones-lista');
  reparacionesError = document.getElementById('reparaciones-error');
  urgenteCheckbox = document.getElementById('urgente');
  btnCotizar = document.getElementById('btn-cotizar');
  btnTexto = document.getElementById('btn-texto');
  btnSpinner = document.getElementById('btn-spinner');
  resultado = document.getElementById('resultado');

  // REQ-007: el botón arranca deshabilitado hasta que el catálogo cargue.
  deshabilitarBotonEnvio();

  // Listeners de cambio de selección: si hay un resultado previo visible, se oculta.
  tipoCalzadoSelect.addEventListener('change', manejarCambioSeleccion);
  urgenteCheckbox.addEventListener('change', manejarCambioSeleccion);
  reparacionesLista.addEventListener('change', manejarCambioSeleccion);

  formCotizacion.addEventListener('submit', manejarEnvioFormulario);

  cargarCatalogos();
}

// Carga en paralelo los catálogos de calzado y reparaciones (REQ-004, REQ-005, REQ-006).
function cargarCatalogos() {
  Promise.all([getTiposCalzado(), getTiposReparacion()])
    .then(([tiposCalzado, tiposReparacion]) => {
      setCatalogos(tiposCalzado, tiposReparacion);
      renderizarSelectCalzado(tiposCalzado);
      renderizarCheckboxesReparaciones(tiposReparacion);
      habilitarBotonEnvio();
    })
    .catch(() => {
      // REQ-006: el mensaje debe indicar que los catálogos no cargaron y que hay
      // que recargar la página. El texto normalizado por api.js (por ejemplo el
      // genérico de un 500) no comunica ninguna de las dos cosas, así que aquí
      // siempre se usa el mensaje fijo del diseño.
      mostrarErrorCatalogo(MENSAJE_ERROR_CATALOGO);
    });
}

// REQ-004, REQ-008: puebla el select con las opciones del catálogo, después de la opción neutra.
function renderizarSelectCalzado(tiposCalzado) {
  tiposCalzado.forEach((tipo) => {
    const option = document.createElement('option');
    option.value = tipo.id;
    option.textContent = tipo.nombre;
    tipoCalzadoSelect.appendChild(option);
  });
}

// REQ-005, REQ-009, REQ-011: crea un checkbox + label por cada tipo de reparación.
function renderizarCheckboxesReparaciones(tiposReparacion) {
  tiposReparacion.forEach((tipo) => {
    const item = document.createElement('div');
    item.className = 'reparacion-item';

    const input = document.createElement('input');
    input.type = 'checkbox';
    input.id = `reparacion-${tipo.id}`;
    input.name = 'tipoReparacionIds';
    input.value = tipo.id;

    const label = document.createElement('label');
    label.setAttribute('for', input.id);
    label.textContent = tipo.nombre;

    item.appendChild(input);
    item.appendChild(label);
    reparacionesLista.appendChild(item);
  });
}

// REQ-014: lee el estado actual del formulario y lo guarda en state.js. Nunca resetea el form.
function leerFormulario() {
  setSeleccion('tipoCalzadoId', tipoCalzadoSelect.value);

  const idsSeleccionados = Array.from(
    reparacionesLista.querySelectorAll('input[name="tipoReparacionIds"]:checked')
  ).map((input) => input.value);
  setSeleccion('tipoReparacionIds', idsSeleccionados);

  setSeleccion('urgente', urgenteCheckbox.checked);
}

// REQ-012, REQ-013: valida la selección actual y devuelve los errores encontrados.
function validarFormulario() {
  const seleccion = getSeleccion();
  const errores = {};

  if (seleccion.tipoCalzadoId === '') {
    errores.tipoCalzado = 'Debe seleccionar un tipo de calzado.';
  }

  if (seleccion.tipoReparacionIds.length === 0) {
    errores.reparaciones = 'Debe elegir al menos un tipo de reparación.';
  }

  return { valido: Object.keys(errores).length === 0, errores };
}

// Pinta los mensajes de error de validación y mueve el foco al primer campo inválido (REQ-033).
function mostrarErroresValidacion(errores) {
  let primerCampoInvalido = null;

  if (errores.tipoCalzado) {
    tipoCalzadoError.textContent = errores.tipoCalzado;
    tipoCalzadoError.classList.add('campo__error--visible');
    tipoCalzadoCampo.classList.add('campo--invalido');
    primerCampoInvalido = primerCampoInvalido || tipoCalzadoSelect;
  }

  if (errores.reparaciones) {
    reparacionesError.textContent = errores.reparaciones;
    reparacionesError.classList.add('campo__error--visible');
    reparacionesFieldset.classList.add('campo--invalido');
    // Un <fieldset> no es enfocable: se enfoca el primer checkbox del grupo.
    const primerCheckbox = reparacionesLista.querySelector('input[type="checkbox"]');
    primerCampoInvalido = primerCampoInvalido || primerCheckbox;
  }

  if (primerCampoInvalido) {
    primerCampoInvalido.focus();
  }
}

// Limpia todos los mensajes y marcas de error de validación.
function limpiarErroresValidacion() {
  tipoCalzadoError.textContent = '';
  tipoCalzadoError.classList.remove('campo__error--visible');
  tipoCalzadoCampo.classList.remove('campo--invalido');

  reparacionesError.textContent = '';
  reparacionesError.classList.remove('campo__error--visible');
  reparacionesFieldset.classList.remove('campo--invalido');
}

// REQ-016: muestra el spinner y cambia el texto del botón mientras se cotiza.
function mostrarSpinner() {
  btnSpinner.hidden = false;
  btnTexto.textContent = 'Cotizando…';
}

// REQ-017: oculta el spinner y restaura el texto del botón.
function ocultarSpinner() {
  btnSpinner.hidden = true;
  btnTexto.textContent = 'Cotizar';
}

// REQ-007, REQ-031: habilita el botón solo si el catálogo se cargó correctamente.
function habilitarBotonEnvio() {
  if (isCatalogoCargado()) {
    btnCotizar.disabled = false;
  }
}

function deshabilitarBotonEnvio() {
  btnCotizar.disabled = true;
}

// REQ-018 a REQ-026: pinta la cotización recibida, reemplazando el contenido previo.
// Consume el contrato real de CotizacionResponse del backend: nombreCalzado,
// reparacionIds (solo ids, sin desglose por línea), nivelUrgencia ('NORMAL'|'URGENTE'),
// subtotal, recargoUrgencia, total, tiempoEstimadoDias. Los nombres de reparación se
// resuelven contra el catálogo ya cargado en state.js (lookup, no cálculo de negocio).
function renderizarResultado(cotizacion) {
  resultado.replaceChildren();

  const esUrgente = cotizacion.nivelUrgencia === 'URGENTE';

  if (esUrgente) {
    const etiqueta = document.createElement('span');
    etiqueta.className = 'etiqueta-urgente';
    etiqueta.textContent = 'Servicio urgente';
    resultado.appendChild(etiqueta);
  }

  const titulo = document.createElement('h2');
  titulo.textContent = `Cotización — ${cotizacion.nombreCalzado}`;
  resultado.appendChild(titulo);

  const { tiposReparacion } = getCatalogos();
  const lista = document.createElement('ul');
  lista.className = 'lista-reparaciones';

  cotizacion.reparacionIds.forEach((id) => {
    const tipo = tiposReparacion.find((reparacion) => reparacion.id === id);
    const item = document.createElement('li');
    item.textContent = tipo ? tipo.nombre : id;
    lista.appendChild(item);
  });
  resultado.appendChild(lista);

  const subtotalParrafo = document.createElement('p');
  subtotalParrafo.className = 'subtotal';
  subtotalParrafo.textContent = `Subtotal: ${cotizacion.subtotal}`;
  resultado.appendChild(subtotalParrafo);

  // REQ-021, REQ-022: el recargo solo se muestra cuando el servicio es urgente.
  if (esUrgente) {
    const recargoParrafo = document.createElement('p');
    recargoParrafo.className = 'recargo';
    recargoParrafo.textContent = `Recargo urgente: ${cotizacion.recargoUrgencia}`;
    resultado.appendChild(recargoParrafo);
  }

  const totalParrafo = document.createElement('p');
  totalParrafo.className = 'total';
  totalParrafo.textContent = `Total: ${cotizacion.total}`;
  resultado.appendChild(totalParrafo);

  const tiempoParrafo = document.createElement('p');
  tiempoParrafo.className = 'tiempo';
  tiempoParrafo.textContent = `Tiempo estimado: ${cotizacion.tiempoEstimadoDias} días`;
  resultado.appendChild(tiempoParrafo);

  resultado.hidden = false;
}

// REQ-027, REQ-028, REQ-029: pinta el mensaje de error ya normalizado por api.js.
function renderizarError(error) {
  resultado.replaceChildren();

  const alerta = document.createElement('p');
  alerta.className = 'alerta alerta--error';
  alerta.textContent = error && error.mensaje ? error.mensaje : MENSAJE_ERROR_GENERICO;
  resultado.appendChild(alerta);

  resultado.hidden = false;
}

// Oculta y vacía el resultado, y limpia la última cotización guardada en el estado.
function ocultarResultado() {
  resultado.replaceChildren();
  resultado.hidden = true;
  setUltimaCotizacion(null);
}

// REQ-006: muestra el mensaje de error de carga de catálogos. El botón sigue deshabilitado.
function mostrarErrorCatalogo(mensaje) {
  catalogoError.textContent = mensaje;
  catalogoError.hidden = false;
}

// Handler de cambio en cualquier campo de selección: sincroniza estado y oculta resultado previo.
function manejarCambioSeleccion() {
  leerFormulario();
  if (getUltimaCotizacion() !== null || resultado.hidden === false) {
    ocultarResultado();
  }
}

// Handler de envío del formulario: valida, cotiza y actualiza la UI según el resultado.
async function manejarEnvioFormulario(evento) {
  evento.preventDefault();

  leerFormulario();
  limpiarErroresValidacion();

  const { valido, errores } = validarFormulario();

  if (!valido) {
    mostrarErroresValidacion(errores);
    return;
  }

  setCargando(true);
  mostrarSpinner();
  deshabilitarBotonEnvio();

  try {
    const cotizacion = await postCotizacion(getSeleccion());
    setUltimaCotizacion(cotizacion);
    renderizarResultado(cotizacion);
  } catch (error) {
    setUltimaCotizacion(null);
    renderizarError(error);
  } finally {
    setCargando(false);
    ocultarSpinner();
    habilitarBotonEnvio();
  }
}

// Registra la inicialización; contempla el caso en que DOMContentLoaded ya haya disparado.
if (document.readyState !== 'loading') {
  inicializar();
} else {
  document.addEventListener('DOMContentLoaded', inicializar);
}
