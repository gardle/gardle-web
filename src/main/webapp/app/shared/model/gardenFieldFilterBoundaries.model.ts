export interface IGardenFieldFilterBoundaries {
  minPrice?: number;
  maxPrice?: number;
  minSize?: number;
  maxSize?: number;
}

export class GardenFieldFilterBoundaries implements IGardenFieldFilterBoundaries {
  constructor(public minPrice?: number, public maxPrice?: number, public minSize?: number, public maxSize?: number) {}
}
