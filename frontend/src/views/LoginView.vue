<script setup>
import AuthLayout from '@/components/layout/AuthLayout.vue'
import AuthCard from '@/components/auth/AuthCard.vue'
import LoginForm from '@/components/auth/LoginForm.vue'
import AuthFooter from '@/components/auth/AuthFooter.vue'
</script>

<template>
  <AuthLayout>
    <AuthCard
      title="Welcome Back"
      subtitle="Sign in to continue"
    >

      <LoginForm />

      <template #footer>
        <AuthFooter
          text="Don't have an account?"
          linkText="Sign up"
          to="/register"
        />
      </template>
    </AuthCard>
  </AuthLayout>
</template>
