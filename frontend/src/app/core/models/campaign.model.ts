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
  contentType: string;
  lastChangeDate: string;
  versionNumber: number;
  status: string;
  currentPhaseId: number;
  contentValue: string;
  festivalName: string;
  festivalLocation: string;
  campaignName: string;
}

export interface CampaignWorkspace {
  campaign: Campaign;
  stats: CampaignStats;
  ads: Ad[];
}

export interface CreativeCampaign {
  campaignId: number;
  campaignName: string;
  festivalName: string;
  festivalLocation: string;
  festivalStatus: string;
  startDate: string;
  endDate: string;
  eligibleAds: number;
}

export interface AdVersionSummary {
  versionNumber: number;
  changedAt: string;
  current: boolean;
}

export interface AdReview {
  ad: Ad;
  flow: AdPhase[];
  versions: AdVersionSummary[];
}

export interface AdVersionDetail {
  adId: number;
  name: string;
  description: string;
  typeName: string;
  contentType: string;
  contentValue: string;
  festivalName: string;
  campaignName: string;
  status: string;
  versionNumber: number;
  changedAt: string;
  current: boolean;
}

export interface StatisticsCampaignOption {
  campaignId: number;
  campaignName: string;
  festivalName: string;
}

export interface StatisticsAdTypeOption {
  adTypeId: number;
  name: string;
}

export interface StatisticsPhaseCount {
  phaseId: number;
  name: string;
  orderIndex: number;
  count: number;
}

export interface StatisticsTypeCount {
  adTypeId: number;
  name: string;
  count: number;
}

export interface StatisticsResponse {
  campaigns: StatisticsCampaignOption[];
  adTypes: StatisticsAdTypeOption[];
  totalAds: number;
  phaseCounts: StatisticsPhaseCount[];
  typeCounts: StatisticsTypeCount[];
}

export interface AdPhase {
  phaseId: number;
  name: string;
  description: string;
  orderIndex: number;
  emailNotification: boolean;
  assignedRole: string;
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
}

export interface CreativeAdUpdateRequest {
  contentValue: string;
}

export interface ManagerAdUpdateRequest {
  name: string;
  description: string;
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
  adTypeId: number | null;
  assignedRole: string;
}
