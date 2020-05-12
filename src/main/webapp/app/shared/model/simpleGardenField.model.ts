export interface ISimpleGardenField {
  id?: number;
  name?: string;
}

export class SimpleGardenField implements ISimpleGardenField {
  constructor(public id?: number, public name?: string) {}
}
