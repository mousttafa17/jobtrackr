import { FormEvent, useState } from "react";
import type { ReactNode } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CheckCircle2, ExternalLink, Pencil, Plus, Trash2, X } from "lucide-react";
import {
  createDocument,
  deleteDocument,
  getDocuments,
} from "../api/documentsApi";
import {
  createInterview,
  deleteInterview,
  getInterviews,
  updateInterview,
} from "../api/interviewsApi";
import { createNote, deleteNote, getNotes } from "../api/notesApi";
import { completeTask, createTask, deleteTask, getTasks } from "../api/tasksApi";
import { Button } from "../components/Button";
import { LoadingState } from "../components/LoadingState";
import { InputField } from "../components/InputField";
import { SelectField } from "../components/SelectField";
import { TextAreaField } from "../components/TextAreaField";
import { useConfirmDialog } from "../components/ConfirmDialogProvider";
import { useToast } from "../components/ToastProvider";
import type {
  CreateInterviewRequest,
  DocumentType,
  Interview,
  InterviewStatus,
  InterviewType,
  UpdateInterviewRequest,
} from "../types/api";
import { getApiErrorMessage } from "../utils/errors";
import {
  formatDateTime,
  formatDocumentType,
  formatInterviewStatus,
  formatInterviewType,
  fromDatetimeLocalValue,
  toDatetimeLocalValue,
} from "../utils/formatters";

const interviewTypes: InterviewType[] = [
  "PHONE_SCREEN",
  "TECHNICAL",
  "BEHAVIORAL",
  "ONSITE",
  "FINAL",
  "HR",
  "OTHER",
];

const interviewStatuses: InterviewStatus[] = [
  "SCHEDULED",
  "COMPLETED",
  "CANCELLED",
  "RESCHEDULED",
];

const documentTypes: DocumentType[] = [
  "RESUME",
  "COVER_LETTER",
  "PORTFOLIO",
  "OFFER_LETTER",
  "JOB_DESCRIPTION",
  "OTHER",
];

type InterviewForm = {
  id?: number;
  type: InterviewType;
  status: InterviewStatus;
  scheduledAt: string;
  location: string;
  meetingLink: string;
  interviewerName: string;
  notes: string;
};

const initialInterviewForm: InterviewForm = {
  type: "TECHNICAL",
  status: "SCHEDULED",
  scheduledAt: "",
  location: "",
  meetingLink: "",
  interviewerName: "",
  notes: "",
};

export function InterviewsTab({ applicationId }: { applicationId: number }) {
  const queryClient = useQueryClient();
  const { confirm } = useConfirmDialog();
  const { showToast } = useToast();
  const [form, setForm] = useState<InterviewForm>(initialInterviewForm);
  const [error, setError] = useState("");

  const query = useQuery({
    queryKey: ["interviews", applicationId],
    queryFn: () => getInterviews(applicationId),
  });

  const createMutation = useMutation({
    mutationFn: (request: CreateInterviewRequest) => createInterview(applicationId, request),
    onSuccess: () => resetAfterSave("Interview added"),
    onError: (apiError) => handleFormError(apiError),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, request }: { id: number; request: UpdateInterviewRequest }) =>
      updateInterview(id, request),
    onSuccess: () => resetAfterSave("Interview updated"),
    onError: (apiError) => handleFormError(apiError),
  });

  const deleteMutation = useMutation({
    mutationFn: deleteInterview,
    onSuccess: () => {
      showToast("Interview deleted");
      void queryClient.invalidateQueries({ queryKey: ["interviews", applicationId] });
    },
    onError: (apiError) => showToast(getApiErrorMessage(apiError), "error"),
  });

  function resetAfterSave(message: string) {
    setForm(initialInterviewForm);
    setError("");
    showToast(message);
    void queryClient.invalidateQueries({ queryKey: ["interviews", applicationId] });
  }

  function handleFormError(apiError: unknown) {
    const message = getApiErrorMessage(apiError);
    setError(message);
    showToast(message, "error");
  }

  async function confirmDelete(id: number) {
    const confirmed = await confirm({
      title: "Delete interview?",
      message: "This interview will be removed from the application.",
      confirmLabel: "Delete",
      dangerous: true,
    });

    if (confirmed) {
      deleteMutation.mutate(id);
    }
  }

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    const scheduledAt = fromDatetimeLocalValue(form.scheduledAt);
    if (!scheduledAt) {
      setError("Scheduled time is required");
      return;
    }

    const request = {
      type: form.type,
      status: form.status,
      scheduledAt,
      location: optionalString(form.location),
      meetingLink: optionalString(form.meetingLink),
      interviewerName: optionalString(form.interviewerName),
      notes: optionalString(form.notes),
    };

    if (form.id) {
      updateMutation.mutate({ id: form.id, request });
    } else {
      createMutation.mutate(request);
    }
  }

  function edit(interview: Interview) {
    setForm({
      id: interview.id,
      type: interview.type,
      status: interview.status,
      scheduledAt: toDatetimeLocalValue(interview.scheduledAt),
      location: interview.location ?? "",
      meetingLink: interview.meetingLink ?? "",
      interviewerName: interview.interviewerName ?? "",
      notes: interview.notes ?? "",
    });
    setError("");
  }

  return (
    <section className="resource-grid">
      <form className="resource-form" onSubmit={submit}>
        <header className="resource-header">
          <h2>{form.id ? "Edit interview" : "Add interview"}</h2>
          {form.id && (
            <button type="button" className="icon-button" onClick={() => setForm(initialInterviewForm)}>
              <X size={18} />
            </button>
          )}
        </header>
        {error && <div className="form-error">{error}</div>}
        <div className="form-grid">
          <SelectField id="interviewType" label="Type" value={form.type} onChange={(event) => setFormField("type", event.target.value as InterviewType)}>
            {interviewTypes.map((type) => <option key={type} value={type}>{formatInterviewType(type)}</option>)}
          </SelectField>
          <SelectField id="interviewStatus" label="Status" value={form.status} onChange={(event) => setFormField("status", event.target.value as InterviewStatus)}>
            {interviewStatuses.map((status) => <option key={status} value={status}>{formatInterviewStatus(status)}</option>)}
          </SelectField>
          <InputField id="scheduledAt" label="Scheduled at" type="datetime-local" value={form.scheduledAt} onChange={(event) => setFormField("scheduledAt", event.target.value)} required />
          <InputField id="interviewerName" label="Interviewer" value={form.interviewerName} onChange={(event) => setFormField("interviewerName", event.target.value)} />
          <InputField id="interviewLocation" label="Location" value={form.location} onChange={(event) => setFormField("location", event.target.value)} />
          <InputField id="meetingLink" label="Meeting link" type="url" value={form.meetingLink} onChange={(event) => setFormField("meetingLink", event.target.value)} />
        </div>
        <TextAreaField id="interviewNotes" label="Notes" value={form.notes} onChange={(event) => setFormField("notes", event.target.value)} />
        <Button type="submit" disabled={createMutation.isPending || updateMutation.isPending}>
          <Plus size={18} />
          {form.id ? "Save interview" : "Add interview"}
        </Button>
      </form>

      <ResourceList isLoading={query.isLoading} error={query.error} emptyText="No interviews yet.">
        {query.data?.map((interview) => (
          <article className="resource-item" key={interview.id}>
            <div>
              <strong>{formatInterviewType(interview.type)}</strong>
              <span>{formatInterviewStatus(interview.status)} · {formatDateTime(interview.scheduledAt)}</span>
              <p>{interview.interviewerName || "No interviewer"} · {interview.location || "No location"}</p>
              {interview.meetingLink && <a href={interview.meetingLink} target="_blank" rel="noreferrer">Meeting link <ExternalLink size={14} /></a>}
            </div>
            <div className="resource-actions">
              <button type="button" className="icon-button" onClick={() => edit(interview)} aria-label="Edit interview"><Pencil size={18} /></button>
              <button type="button" className="icon-button" onClick={() => void confirmDelete(interview.id)} aria-label="Delete interview"><Trash2 size={18} /></button>
            </div>
          </article>
        ))}
      </ResourceList>
    </section>
  );

  function setFormField<Key extends keyof InterviewForm>(key: Key, value: InterviewForm[Key]) {
    setForm((current) => ({ ...current, [key]: value }));
  }
}

export function TasksTab({ applicationId }: { applicationId: number }) {
  const queryClient = useQueryClient();
  const { confirm } = useConfirmDialog();
  const { showToast } = useToast();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [dueAt, setDueAt] = useState("");
  const [error, setError] = useState("");

  const query = useQuery({ queryKey: ["tasks", applicationId], queryFn: () => getTasks(applicationId) });
  const createMutation = useMutation({
    mutationFn: () => createTask(applicationId, { title, description: optionalString(description), dueAt: fromDatetimeLocalValue(dueAt) }),
    onSuccess: () => {
      setTitle("");
      setDescription("");
      setDueAt("");
      setError("");
      showToast("Task added");
      void queryClient.invalidateQueries({ queryKey: ["tasks", applicationId] });
    },
    onError: (apiError) => {
      const message = getApiErrorMessage(apiError);
      setError(message);
      showToast(message, "error");
    },
  });
  const completeMutation = useMutation({
    mutationFn: completeTask,
    onSuccess: () => {
      showToast("Task completed");
      void queryClient.invalidateQueries({ queryKey: ["tasks", applicationId] });
    },
    onError: (apiError) => showToast(getApiErrorMessage(apiError), "error"),
  });
  const deleteMutation = useMutation({
    mutationFn: deleteTask,
    onSuccess: () => {
      showToast("Task deleted");
      void queryClient.invalidateQueries({ queryKey: ["tasks", applicationId] });
    },
    onError: (apiError) => showToast(getApiErrorMessage(apiError), "error"),
  });

  async function confirmDelete(id: number) {
    const confirmed = await confirm({
      title: "Delete task?",
      message: "This task will be removed from the application.",
      confirmLabel: "Delete",
      dangerous: true,
    });

    if (confirmed) {
      deleteMutation.mutate(id);
    }
  }

  return (
    <section className="resource-grid">
      <form className="resource-form" onSubmit={(event) => { event.preventDefault(); createMutation.mutate(); }}>
        <h2>Add task</h2>
        {error && <div className="form-error">{error}</div>}
        <InputField id="taskTitle" label="Title" value={title} onChange={(event) => setTitle(event.target.value)} required />
        <InputField id="taskDueAt" label="Due at" type="datetime-local" value={dueAt} onChange={(event) => setDueAt(event.target.value)} />
        <TextAreaField id="taskDescription" label="Description" value={description} onChange={(event) => setDescription(event.target.value)} />
        <Button type="submit" disabled={createMutation.isPending}><Plus size={18} />Add task</Button>
      </form>
      <ResourceList isLoading={query.isLoading} error={query.error} emptyText="No tasks yet.">
        {query.data?.map((task) => (
          <article className={`resource-item ${task.completed ? "completed" : ""}`} key={task.id}>
            <div>
              <strong>{task.title}</strong>
              <span>{task.dueAt ? `Due ${formatDateTime(task.dueAt)}` : "No due date"}</span>
              <p>{task.description || "No description"}</p>
            </div>
            <div className="resource-actions">
              {!task.completed && <button type="button" className="icon-button" onClick={() => completeMutation.mutate(task.id)} aria-label="Complete task"><CheckCircle2 size={18} /></button>}
              <button type="button" className="icon-button" onClick={() => void confirmDelete(task.id)} aria-label="Delete task"><Trash2 size={18} /></button>
            </div>
          </article>
        ))}
      </ResourceList>
    </section>
  );
}

export function NotesTab({ applicationId }: { applicationId: number }) {
  const queryClient = useQueryClient();
  const { confirm } = useConfirmDialog();
  const { showToast } = useToast();
  const [content, setContent] = useState("");
  const [error, setError] = useState("");
  const query = useQuery({ queryKey: ["notes", applicationId], queryFn: () => getNotes(applicationId) });
  const createMutation = useMutation({
    mutationFn: () => createNote(applicationId, { content }),
    onSuccess: () => {
      setContent("");
      setError("");
      showToast("Note added");
      void queryClient.invalidateQueries({ queryKey: ["notes", applicationId] });
    },
    onError: (apiError) => {
      const message = getApiErrorMessage(apiError);
      setError(message);
      showToast(message, "error");
    },
  });
  const deleteMutation = useMutation({
    mutationFn: deleteNote,
    onSuccess: () => {
      showToast("Note deleted");
      void queryClient.invalidateQueries({ queryKey: ["notes", applicationId] });
    },
    onError: (apiError) => showToast(getApiErrorMessage(apiError), "error"),
  });

  async function confirmDelete(id: number) {
    const confirmed = await confirm({
      title: "Delete note?",
      message: "This note will be removed from the application.",
      confirmLabel: "Delete",
      dangerous: true,
    });

    if (confirmed) {
      deleteMutation.mutate(id);
    }
  }

  return (
    <section className="resource-grid">
      <form className="resource-form" onSubmit={(event) => { event.preventDefault(); createMutation.mutate(); }}>
        <h2>Add note</h2>
        {error && <div className="form-error">{error}</div>}
        <TextAreaField id="noteContent" label="Content" value={content} onChange={(event) => setContent(event.target.value)} required />
        <Button type="submit" disabled={createMutation.isPending}><Plus size={18} />Add note</Button>
      </form>
      <ResourceList isLoading={query.isLoading} error={query.error} emptyText="No notes yet.">
        {query.data?.map((note) => (
          <article className="resource-item" key={note.id}>
            <div>
              <strong>{formatDateTime(note.createdAt)}</strong>
              <p>{note.content}</p>
            </div>
            <button type="button" className="icon-button" onClick={() => void confirmDelete(note.id)} aria-label="Delete note"><Trash2 size={18} /></button>
          </article>
        ))}
      </ResourceList>
    </section>
  );
}

export function DocumentsTab({ applicationId }: { applicationId: number }) {
  const queryClient = useQueryClient();
  const { confirm } = useConfirmDialog();
  const { showToast } = useToast();
  const [name, setName] = useState("");
  const [type, setType] = useState<DocumentType>("RESUME");
  const [url, setUrl] = useState("");
  const [notes, setNotes] = useState("");
  const [error, setError] = useState("");
  const query = useQuery({ queryKey: ["documents", applicationId], queryFn: () => getDocuments(applicationId) });
  const createMutation = useMutation({
    mutationFn: () => createDocument(applicationId, { name, type, url, notes: optionalString(notes) }),
    onSuccess: () => {
      setName("");
      setType("RESUME");
      setUrl("");
      setNotes("");
      setError("");
      showToast("Document added");
      void queryClient.invalidateQueries({ queryKey: ["documents", applicationId] });
    },
    onError: (apiError) => {
      const message = getApiErrorMessage(apiError);
      setError(message);
      showToast(message, "error");
    },
  });
  const deleteMutation = useMutation({
    mutationFn: deleteDocument,
    onSuccess: () => {
      showToast("Document deleted");
      void queryClient.invalidateQueries({ queryKey: ["documents", applicationId] });
    },
    onError: (apiError) => showToast(getApiErrorMessage(apiError), "error"),
  });

  async function confirmDelete(id: number) {
    const confirmed = await confirm({
      title: "Delete document?",
      message: "This document link will be removed from the application.",
      confirmLabel: "Delete",
      dangerous: true,
    });

    if (confirmed) {
      deleteMutation.mutate(id);
    }
  }

  return (
    <section className="resource-grid">
      <form className="resource-form" onSubmit={(event) => { event.preventDefault(); createMutation.mutate(); }}>
        <h2>Add document</h2>
        {error && <div className="form-error">{error}</div>}
        <div className="form-grid">
          <InputField id="documentName" label="Name" value={name} onChange={(event) => setName(event.target.value)} required />
          <SelectField id="documentType" label="Type" value={type} onChange={(event) => setType(event.target.value as DocumentType)}>
            {documentTypes.map((item) => <option key={item} value={item}>{formatDocumentType(item)}</option>)}
          </SelectField>
        </div>
        <InputField id="documentUrl" label="URL" type="url" value={url} onChange={(event) => setUrl(event.target.value)} required />
        <TextAreaField id="documentNotes" label="Notes" value={notes} onChange={(event) => setNotes(event.target.value)} />
        <Button type="submit" disabled={createMutation.isPending}><Plus size={18} />Add document</Button>
      </form>
      <ResourceList isLoading={query.isLoading} error={query.error} emptyText="No documents yet.">
        {query.data?.map((document) => (
          <article className="resource-item" key={document.id}>
            <div>
              <strong>{document.name}</strong>
              <span>{formatDocumentType(document.type)} · {formatDateTime(document.createdAt)}</span>
              <p>{document.notes || "No notes"}</p>
              <a href={document.url} target="_blank" rel="noreferrer">Open document <ExternalLink size={14} /></a>
            </div>
            <button type="button" className="icon-button" onClick={() => void confirmDelete(document.id)} aria-label="Delete document"><Trash2 size={18} /></button>
          </article>
        ))}
      </ResourceList>
    </section>
  );
}

function ResourceList({
  isLoading,
  error,
  emptyText,
  children,
}: {
  isLoading: boolean;
  error: unknown;
  emptyText: string;
  children: ReactNode;
}) {
  const items = Array.isArray(children) ? children.filter(Boolean) : children;
  if (isLoading) {
    return <section className="resource-list muted-panel"><LoadingState /></section>;
  }
  if (error) {
    return <section className="form-error">{getApiErrorMessage(error)}</section>;
  }
  if (!items || (Array.isArray(items) && items.length === 0)) {
    return <section className="resource-list muted-panel">{emptyText}</section>;
  }
  return <section className="resource-list">{children}</section>;
}

function optionalString(value: string): string | null {
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
}
