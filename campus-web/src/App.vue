<template>
  <router-view />
  <LoginDialog v-model="authDialog.visible" @success="authDialog.handleSuccess" />
</template>

<script setup lang="ts">
import LoginDialog from '@/components/LoginDialog.vue'
import { useAuthDialogStore } from '@/stores/authDialog'

const authDialog = useAuthDialogStore()
</script>
