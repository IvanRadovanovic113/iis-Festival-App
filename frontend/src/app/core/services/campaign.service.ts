import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Ad,
  AdPhase,
  AdPhaseRequest,
  AdReview,
  AdRequest,
  AdType,
  AdTypeRequest,
  AdVersionDetail,
  Campaign,
  CampaignManagerOption,
  CampaignRequest,
  CampaignWorkspace,
  CreativeAdUpdateRequest,
  FestivalCampaignOverview,
  ManagerAdUpdateRequest,
  StatisticsResponse
} from '../models/campaign.model';

@Injectable({ providedIn: 'root' })
export class CampaignService {
  private readonly http = inject(HttpClient);
  private readonly DIRECTOR_API = '/api/festival-director';
  private readonly MANAGER_API = '/api/festival-manager';
  private readonly CREATIVE_API = '/api/creative';

  getDirectorFestivalOverviews(): Observable<FestivalCampaignOverview[]> {
    return this.http.get<FestivalCampaignOverview[]>(`${this.DIRECTOR_API}/festivals`);
  }

  getDirectorCampaign(festivalId: number): Observable<Campaign> {
    return this.http.get<Campaign>(`${this.DIRECTOR_API}/festivals/${festivalId}/campaign`);
  }

  getDirectorCampaignWorkspace(festivalId: number): Observable<CampaignWorkspace> {
    return this.http.get<CampaignWorkspace>(`${this.DIRECTOR_API}/festivals/${festivalId}/campaign/workspace`);
  }

  createCampaign(festivalId: number, request: CampaignRequest): Observable<Campaign> {
    return this.http.post<Campaign>(`${this.DIRECTOR_API}/festivals/${festivalId}/campaign`, request);
  }

  getFestivalManagers(festivalId: number): Observable<CampaignManagerOption[]> {
    return this.http.get<CampaignManagerOption[]>(`${this.DIRECTOR_API}/festivals/${festivalId}/managers`);
  }

  getManagerFestivalOverviews(): Observable<FestivalCampaignOverview[]> {
    return this.http.get<FestivalCampaignOverview[]>(`${this.MANAGER_API}/festivals`);
  }

  getManagerCampaignWorkspace(festivalId: number): Observable<CampaignWorkspace> {
    return this.http.get<CampaignWorkspace>(`${this.MANAGER_API}/festivals/${festivalId}/campaign`);
  }

  getAdTypes(): Observable<AdType[]> {
    return this.http.get<AdType[]>(`${this.MANAGER_API}/ad-types`);
  }

  getAdPhases(): Observable<AdPhase[]> {
    return this.http.get<AdPhase[]>(`${this.MANAGER_API}/ad-phases`);
  }

  createAd(campaignId: number, request: AdRequest) {
    return this.http.post(`${this.MANAGER_API}/campaigns/${campaignId}/ads`, request);
  }

  getManagerAd(festivalId: number, adId: number) {
    return this.http.get<Ad>(`${this.MANAGER_API}/festivals/${festivalId}/ads/${adId}`);
  }

  updateManagerAd(festivalId: number, adId: number, request: ManagerAdUpdateRequest) {
    return this.http.put<Ad>(`${this.MANAGER_API}/festivals/${festivalId}/ads/${adId}`, request);
  }

  createAdType(request: AdTypeRequest) {
    return this.http.post(`${this.MANAGER_API}/ad-types`, request);
  }

  createAdPhase(request: AdPhaseRequest) {
    return this.http.post(`${this.MANAGER_API}/ad-phases`, request);
  }

  getCreativeAds() {
    return this.http.get<Ad[]>(`${this.CREATIVE_API}/ads`);
  }

  getCreativeAd(adId: number) {
    return this.http.get<Ad>(`${this.CREATIVE_API}/ads/${adId}`);
  }

  updateCreativeAd(adId: number, request: CreativeAdUpdateRequest) {
    return this.http.put<Ad>(`${this.CREATIVE_API}/ads/${adId}`, request);
  }

  getAdReview(festivalId: number, adId: number) {
    return this.http.get<AdReview>(`/api/ad-reviews/festivals/${festivalId}/ads/${adId}`);
  }

  getAdVersionDetail(festivalId: number, adId: number, versionNumber: number) {
    return this.http.get<AdVersionDetail>(`/api/ad-reviews/festivals/${festivalId}/ads/${adId}/versions/${versionNumber}`);
  }

  getStatistics(params: {
    campaignId?: number | null;
    dateFrom?: string | null;
    dateTo?: string | null;
    adTypeId?: number | null;
  }) {
    const query = new URLSearchParams();
    if (params.campaignId) query.set('campaignId', String(params.campaignId));
    if (params.dateFrom) query.set('dateFrom', params.dateFrom);
    if (params.dateTo) query.set('dateTo', params.dateTo);
    if (params.adTypeId) query.set('adTypeId', String(params.adTypeId));
    const suffix = query.toString() ? `?${query.toString()}` : '';
    return this.http.get<StatisticsResponse>(`/api/statistics${suffix}`);
  }
}
