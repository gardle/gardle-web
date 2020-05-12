import { IGardenField } from '@/shared/model/gardenfield.model';
import { PageModel } from '@/shared/model/page.model';

export interface IGardenFieldPage {
  content?: IGardenField[];
  pageable?: PageModel;
  totalPages?: number;
}

export class GardenFieldPage implements IGardenFieldPage {
  constructor(public pageable?: PageModel, public totalPages?: number, public content?: IGardenField[]) {}
}
