const TOKEN_KEY = 'gimnasio_token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

async function request(path, { method = 'GET', body, params } = {}) {
  let url = `/api${path}`
  if (params) {
    const query = new URLSearchParams(
      Object.entries(params).filter(([, valor]) => valor !== undefined && valor !== null && valor !== ''),
    )
    if (query.toString()) {
      url += `?${query.toString()}`
    }
  }

  const headers = { 'Content-Type': 'application/json' }
  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const respuesta = await fetch(url, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  if (respuesta.status === 401) {
    setToken(null)
    localStorage.removeItem('gimnasio_usuario')
    if (!window.location.pathname.startsWith('/login')) {
      window.location.href = '/login'
    }
    throw new Error('Sesión expirada; inicie sesión de nuevo')
  }

  if (respuesta.status === 204) {
    return null
  }

  const texto = await respuesta.text()
  let datos = null
  if (texto) {
    try {
      datos = JSON.parse(texto)
    } catch {
      datos = null
    }
  }

  if (!respuesta.ok) {
    const mensaje =
      (datos && (datos.error || datos.message)) ||
      (datos && datos.campos ? Object.values(datos.campos).join('. ') : null) ||
      `Error ${respuesta.status}`
    throw new Error(mensaje)
  }
  return datos
}

export const api = {
  get: (path, params) => request(path, { params }),
  post: (path, body) => request(path, { method: 'POST', body }),
  put: (path, body) => request(path, { method: 'PUT', body }),
  del: (path) => request(path, { method: 'DELETE' }),
}
