export function escapeHtml(unsafe): string {
  return unsafe
    .replace(/&(?!amp;|lt;|gt;|quot;|#039;)/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

export function escapeData(obj) {
  for (const k in obj) {
    if (!obj.hasOwnProperty(k)) {
      continue;
    }
    if (typeof obj[k] === 'object' && obj[k] !== null) {
      escapeData(obj[k]);
    } else if (typeof obj[k] === 'string' && !whiteListedProperties.includes(obj[k])) {
      obj[k] = escapeHtml(obj[k]);
    }
  }
  return obj;
}

const whiteListedProperties = ['password', 'currentPassword', 'newPassword'];
