export interface FestivalCampaignOverview {
  festivalId: number;
  name: string;
  location: string;
  status: string;
  startDate: string;
  endDate: string;
  hasCampaign: boolean;
  campaignId: number | null;
  campaignName: string | null;
  managerName: string | null;
}

export interface CampaignManagerOption {
  userId: number;
  username: string;
  email: string;
}

export interface Campaign {
  campaignId: number;
  festivalId: number;
  festivalName: string;
  festivalLocation: string;
  festivalStatus: string;
  name: string;
  description: string;
  startDate: string;
  endDate: string;
  managerUserId: number;
  managerName: string;
}

export interface CampaignRequest {
  name: string;
  description: string;
  startDate: string;
  endDate: string;
  managerUserId: number;
}

export interface CampaignStats {
  totalAds: number;
  todo: number;
  draft: number;
  approvedTechnical: number;
  approved: number;
  rejected: number;
}

export interface Ad {
  adId: number;
  name: string;
  description: string;
  typeName: string;
  adTypeId: number;
  lastChangeDate: string;
  versionNumber: number;
  status: string;
  contentFileName: string;
}

export interface CampaignWorkspace {
  campaign: Campaign;
  stats: CampaignStats;
  ads: Ad[];
}

export interface AdPhase {
  phaseId: number;
  name: string;
  description: string;
  orderIndex: number;
  emailNotification: boolean;
}

export interface AdType {
  adTypeId: number;
  name: string;
  description: string;
  contentType: string;
  phases: AdPhase[];
}

export interface AdRequest {
  name: string;
  description: string;
  adTypeId: number;
  contentFileName: string;
}

export interface AdTypeRequest {
  name: string;
  description: string;
  contentType: string;
  phaseIds: number[];
}

export interface AdPhaseRequest {
  name: string;
  description: string;
  orderIndex: number;
  emailNotification: boolean;
  adTypeId: number;
}
