import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { WorkflowTemplateService } from '../../../../core/services/workflow-template.service';
import { ConditionCatalogService } from '../../../../core/services/condition-catalog.service';
import { TransitionConditionResponse, ConditionDataType } from '../../../../core/models/workflow.model';

@Component({
  selector: 'app-workflow-template-form',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule],
  templateUrl: './workflow-template-form.component.html',
  styleUrls: ['./workflow-template-form.component.css']
})
export class WorkflowTemplateFormComponent implements OnInit {
  workflowForm!: FormGroup;
  catalogConditions: TransitionConditionResponse[] = [];
  isSubmitting = false;
  errorMessage = '';
  draggedIndex: number | null = null;

  // Modal State
  isModalOpen = false;
  activeTransitionIndexForModal: number | null = null;
  newCondition = {
    conditionKey: '',
    dataType: 'BOOLEAN' as ConditionDataType,
    label: 'descripiton',
    necessary: false
  };

  constructor(
    private fb: FormBuilder,
    private workflowService: WorkflowTemplateService,
    private catalogService: ConditionCatalogService,
    private router: Router,
    private route: ActivatedRoute // Dodato za čitanje query parametara
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadCatalogConditions(() => {
      // Tek kada učitamo katalog, proveravamo da li radimo kopiranje/novu verziju
      this.checkAndCloneTemplate();
    });
  }

  private initForm(): void {
    this.workflowForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      states: this.fb.array([]),
      transitions: this.fb.array([])
    });
  }

private checkAndCloneTemplate(): void {
    const cloneFromId = this.route.snapshot.queryParamMap.get('cloneFrom');
    if (cloneFromId) {
      this.workflowService.getTemplateById(+cloneFromId).subscribe({
        next: (details) => {
          this.workflowForm.get('name')?.setValue(`${details.name} (New Version)`);
          
          const sortedStates = [...details.states].sort((a, b) => {
            if (a.initial) return -1;
            if (b.initial) return 1;
            if (a.finalState) return 1;
            if (b.finalState) return -1;
            return 0;
          });

          sortedStates.forEach(s => {
            this.states.push(this.fb.group({
              name: [s.name, [Validators.required]],
              initial: [s.initial],
              finalState: [s.finalState],
              defaultDeadlineDays: [s.defaultDeadlineDays, [Validators.required, Validators.min(1)]]
            }));
          });

          details.transitions.forEach(t => {
            const sourceState = details.states.find(s => s.id === t.sourceStateId);
            const targetState = details.states.find(s => s.id === t.targetStateId);
            
            const conditionIds = t.conditions ? t.conditions.map(c => c.id) : [];
            
            this.transitions.push(this.fb.group({
              label: [t.label, [Validators.required]],
              sourceStateName: [sourceState ? sourceState.name : '', [Validators.required]],
              targetStateName: [targetState ? targetState.name : '', [Validators.required]],
              conditionIds: [conditionIds]
            }));
          });
        },
        error: (err) => console.error('Failed to load template for cloning', err)
      });
    } else {
      this.addInitialAndFinalStates();
    }
  }

  private addInitialAndFinalStates(): void {
    this.states.push(this.fb.group({
      name: ['Initial Contact', [Validators.required]],
      initial: [true],
      finalState: [false],
      defaultDeadlineDays: [2, [Validators.required, Validators.min(1)]]
    }));

    this.states.push(this.fb.group({
      name: ['Contract Signed', [Validators.required]],
      initial: [false],
      finalState: [true],
      defaultDeadlineDays: [1, [Validators.required, Validators.min(1)]]
    }));
  }

  loadCatalogConditions(callback?: () => void): void {
    this.catalogService.getCatalogConditions('', 0, 100).subscribe({
      next: (res) => {
        this.catalogConditions = res.content;
        if (callback) callback();
      },
      error: (err) => console.error('Failed to load conditions catalog', err)
    });
  }

  get states(): FormArray {
    return this.workflowForm.get('states') as FormArray;
  }

  get transitions(): FormArray {
    return this.workflowForm.get('transitions') as FormArray;
  }

  addState(name = '', deadlineDays = 2): void {
    const stateGroup = this.fb.group({
      name: [name, [Validators.required]],
      initial: [false],
      finalState: [false],
      defaultDeadlineDays: [deadlineDays, [Validators.required, Validators.min(1)]]
    });

    if (this.states.length >= 2) {
      this.states.insert(this.states.length - 1, stateGroup);
    } else {
      this.states.push(stateGroup);
    }
  }

  removeState(index: number): void {
    if (index === 0 || index === this.states.length - 1) return;
    this.states.removeAt(index);
  }

  addTransition(): void {
    const transitionGroup = this.fb.group({
      label: ['', [Validators.required]],
      sourceStateName: ['', [Validators.required]],
      targetStateName: ['', [Validators.required]],
      conditionIds: [[]]
    });
    this.transitions.push(transitionGroup);
  }

  removeTransition(index: number): void {
    this.transitions.removeAt(index);
  }

  onConditionToggle(transitionIndex: number, conditionId: number, event: Event): void {
    const checkbox = event.target as HTMLInputElement;
    const transitionFormGroup = this.transitions.at(transitionIndex);
    let currentIds: number[] = transitionFormGroup.get('conditionIds')?.value || [];

    if (checkbox.checked) {
      currentIds = [...currentIds, conditionId];
    } else {
      currentIds = currentIds.filter(id => id !== conditionId);
    }
    transitionFormGroup.get('conditionIds')?.setValue(currentIds);
  }

  isConditionChecked(transitionIndex: number, conditionId: number): boolean {
    const currentIds: number[] = this.transitions.at(transitionIndex).get('conditionIds')?.value || [];
    return currentIds.includes(conditionId);
  }

  // --- Modal Logic ---
openNewConditionModal(transitionIndex: number): void {
    this.activeTransitionIndexForModal = transitionIndex;
    this.newCondition = {
      conditionKey: 'COND_', 
      dataType: 'BOOLEAN' as ConditionDataType,
      label: '',
      necessary: false
    };
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.activeTransitionIndexForModal = null;
  }

  saveCustomCondition(): void {
    if (!this.newCondition.label.trim() || !this.newCondition.conditionKey.trim()) {
      alert('Please fill in all required fields.');
      return;
    }
    
    this.newCondition.conditionKey = this.newCondition.conditionKey.trim().toUpperCase().replace(/\s+/g, '_');

    this.catalogService.createCatalogCondition(this.newCondition).subscribe({
      next: (savedCond) => {
        // Ponovo učitaj katalog da se pojavi u svim granama
        this.loadCatalogConditions(() => {
          // Automatski čekiraj novo kreirani uslov u tranziciji iz koje je modal otvoren
          if (this.activeTransitionIndexForModal !== null) {
            const transFormGroup = this.transitions.at(this.activeTransitionIndexForModal);
            const currentIds: number[] = transFormGroup.get('conditionIds')?.value || [];
            transFormGroup.get('conditionIds')?.setValue([...currentIds, savedCond.id]);
          }
          this.closeModal();
        });
      },
      error: (err) => {
        console.error('Backend error details:', err);
        const serverMessage = err.error?.message || err.message || 'Unknown server error';
        alert(`Failed to save condition. Server responds: ${serverMessage}`);
      }
    });
  }

  // --- Drag and Drop ---
  onDragStart(index: number): void {
    if (index === 0 || index === this.states.length - 1) return;
    this.draggedIndex = index;
  }

  onDragOver(event: DragEvent, index: number): void {
    if (index === 0 || index === this.states.length - 1) return;
    event.preventDefault();
  }

  onDrop(index: number): void {
    if (this.draggedIndex === null || this.draggedIndex === index) return;
    if (index === 0 || index === this.states.length - 1) return;

    const draggedTarget = this.states.at(this.draggedIndex);
    this.states.removeAt(this.draggedIndex);
    this.states.insert(index, draggedTarget);
    this.draggedIndex = null;
  }

  onSubmit(): void {
    this.errorMessage = '';
    if (this.workflowForm.invalid) {
      this.workflowForm.markAllAsTouched();
      return;
    }

    const formValue = this.workflowForm.value;
    formValue.name = formValue.name.trim();
    formValue.states.forEach((s: any) => s.name = s.name.trim());
    formValue.transitions.forEach((t: any) => t.label = t.label.trim());

    const hasInitial = formValue.states.some((s: any) => s.initial === true);
    const hasFinal = formValue.states.some((s: any) => s.finalState === true);

    if (!hasInitial || !hasFinal) {
      this.errorMessage = 'Business Rule Violation: MUST have at least one INITIAL and one FINAL state.';
      return;
    }

    if (formValue.transitions.length === 0) {
      this.errorMessage = 'Business Rule Violation: You must define at least one transition branch.';
      return;
    }

    this.isSubmitting = true;
    this.workflowService.createTemplate(formValue).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.router.navigate(['/negotiation-manager/workflow-templates']);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || 'An error occurred while saving.';
      }
    });
  }
}