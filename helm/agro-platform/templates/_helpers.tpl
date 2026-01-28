{{- define "agro-platform.name" -}}
{{- .name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "agro-platform.fullname" -}}
{{- printf "%s-%s" .root.Release.Name .name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "agro-platform.labels" -}}
app.kubernetes.io/name: {{ include "agro-platform.name" . }}
app.kubernetes.io/instance: {{ .root.Release.Name }}
app.kubernetes.io/version: {{ .root.Chart.AppVersion }}
app.kubernetes.io/managed-by: {{ .root.Release.Service }}
app.kubernetes.io/component: {{ .name }}
{{- end -}}
