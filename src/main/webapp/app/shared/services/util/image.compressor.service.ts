// This is a adapted version from an awesome stackoverflow post https://stackoverflow.com/a/39235724

import { ImageFileNotValidError } from '@/shared/exceptions';

export default class ImageCompressorService {
  public async compressImage(imageOptions: IResizeImageOptions): Promise<File> {
    const { imageFile } = imageOptions;
    const compressedBlob: Blob = await resizeImage(imageOptions);
    return new File([compressedBlob], 'compressed_' + imageFile.name + '.jpg', {
      lastModified: imageFile.lastModified,
      type: imageFile.type
    });
  }
}

export interface IResizeImageOptions {
  maxSize: number;
  imageFile: File;
  quality: number; // number between 0-1 to specify jpeg quality
}

const resizeImage = (settings: IResizeImageOptions): Promise<Blob> => {
  return new Promise((resolve, reject) => {
    const { imageFile, maxSize, quality } = settings;
    if (!imageFile.type.match(/image.*/)) {
      reject(new ImageFileNotValidError());
      return;
    }
    const reader = new FileReader();
    reader.onload = (readerEvent: any) => {
      const image = new Image();
      image.onload = () => resolve(resize(image, maxSize, quality));
      image.src = readerEvent.target.result;
    };
    reader.readAsDataURL(imageFile);
  });
};

const resize = (image: HTMLImageElement, maxSize: number, quality: number) => {
  const canvas = document.createElement('canvas');
  const { width, height } = calcDimensions(image.width, image.height, maxSize);
  canvas.width = width;
  canvas.height = height;
  canvas.getContext('2d').drawImage(image, 0, 0, width, height);
  const dataUrl = canvas.toDataURL('image/jpeg', quality);
  return dataURItoBlob(dataUrl);
};

const calcDimensions = (width: number, height: number, maxSize: number) => {
  if (width > height) {
    if (width > maxSize) {
      height *= maxSize / width;
      width = maxSize;
    }
  } else {
    if (height > maxSize) {
      width *= maxSize / height;
      height = maxSize;
    }
  }
  return { width, height };
};

const dataURItoBlob = (dataURI: string) => {
  const bytes = dataURI.split(',')[0].indexOf('base64') >= 0 ? atob(dataURI.split(',')[1]) : unescape(dataURI.split(',')[1]);
  const mime = dataURI
    .split(',')[0]
    .split(':')[1]
    .split(';')[0];
  const max = bytes.length;
  const ia = new Uint8Array(max);
  for (let i = 0; i < max; i++) {
    ia[i] = bytes.charCodeAt(i);
  }
  return new Blob([ia], { type: mime });
};
