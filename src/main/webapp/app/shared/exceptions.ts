import { AxiosResponse } from 'axios';

export class NullArgumentError extends Error {
  constructor(message?) {
    super(message);
    this.name = 'NullArgumentError';
  }
}

export class RenderableError extends Error {
  level: string;
  stack: string;
  title_key: string;

  protected constructor(title_key, level, message_key?) {
    super(message_key);
    this.title_key = title_key;
    this.level = level;
  }
}

export class ImageFileNotValidError extends RenderableError {
  constructor(title_key = 'IMAGE_INVALID', level = 'error', message_key?: string) {
    super(title_key, level, message_key);
    this.name = 'ImageFileNotValidError';
  }
}

export class GardleAPIException extends RenderableError {
  public icon: string;

  constructor(title_key, level = 'error', icon?: string, message_key?: string) {
    super(title_key, level, message_key);
    this.icon = icon;
    this.name = 'GardleAPIException';
  }
}

export function mapException(err: AxiosResponse) {
  const attrs = {
    title: err.data.title || 'UNKNOWN_ERROR',
    level: null,
    icon: null
  };

  switch (err.status) {
    case 400:
      attrs.level = 'warning';
      attrs.icon = 'mdi-alert';
      break;
    case 401:
      attrs.level = 'warning';
      attrs.icon = 'mdi-logout';
      break;
    case 403:
      attrs.level = 'error';
      attrs.icon = 'mdi-cancel';
      break;
    case 404:
      attrs.level = 'warning';
      attrs.icon = 'mdi-cloud-question';
      break;
    case 409:
      attrs.level = 'error';
      attrs.icon = 'mdi-flash';
      break;
    case 500:
      attrs.level = 'error';
      attrs.icon = 'mdi-server';
      break;
  }
  return new GardleAPIException(attrs.title, attrs.level, attrs.icon);
}
